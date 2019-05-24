package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;
import at.ac.tuwien.kr.alpha.common.depgraph.StratificationHelper;
import at.ac.tuwien.kr.alpha.common.depgraph.StronglyConnectedComponentsHelper;
import at.ac.tuwien.kr.alpha.common.depgraph.StronglyConnectedComponentsHelper.SCCResult;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrder;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.transformation.ProgramTransformation;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

/**
 * Evaluates the stratifiable part (if any) of the given program
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO maybe use NormalProgram as input type, could do dependency graph right here
// TODO ideally return "PartiallyEvaluatedProgram" here, grounder can use working memories created here rather than re-initialize everything
// TODO add solved rules to internal program (in extra list)
public class PartialEvaluation extends ProgramTransformation<InternalProgram, InternalProgram> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartialEvaluation.class);

	private StronglyConnectedComponentsHelper sccHelper = new StronglyConnectedComponentsHelper();
	private StratificationHelper stratificationHelper = new StratificationHelper();

	private WorkingMemory workingMemory = new WorkingMemory();
	private Map<Predicate, HashSet<InternalRule>> predicateDefiningRules;
	private Map<Predicate, LinkedHashSet<Instance>> programFacts;

	// context settings for bindNextAtom - TODO make better
	private boolean stopBindingAtNonTruePositiveBody = true;
	private AtomStore atomStore = new AtomStoreImpl();
	private int maxAtomIdBeforeGroundingNewNoGoods = -1;
	private LinkedHashSet<Atom> removeAfterObtainingNewNoGoods = new LinkedHashSet<>();
	private boolean disableInstanceRemoval;

	private Set<Atom> additionalFacts = new HashSet<>();
	private Set<Integer> solvedRuleIds = new HashSet<>();

	@Override
	public InternalProgram apply(InternalProgram inputProgram) {
		// first, we calculate a component graph and stratification
		SCCResult sccResult = this.sccHelper.findStronglyConnectedComponents(inputProgram.getDependencyGraph());
		ComponentGraph componentGraph = ComponentGraph.buildComponentGraph(inputProgram.getDependencyGraph(), sccResult);
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(componentGraph);
		this.predicateDefiningRules = inputProgram.getPredicateDefiningRules();
		this.programFacts = inputProgram.getFactsByPredicate();
		// set up list of atoms which are known to be true - we expand on this one
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		for (Map.Entry<Predicate, Set<Instance>> entry : knownFacts.entrySet()) {
			this.workingMemory.initialize(entry.getKey());
			this.workingMemory.addInstances(entry.getKey(), true, entry.getValue());
		}

		ComponentEvaluationOrder evaluationOrder = new ComponentEvaluationOrder(strata);
		for (SCComponent currComponent : evaluationOrder) {
			this.evaluateComponent(currComponent);
		}

		// build the resulting program
		List<Atom> outputFacts = new ArrayList<>(inputProgram.getFacts());
		outputFacts.addAll(this.additionalFacts);
		List<InternalRule> outputRules = new ArrayList<>();
		inputProgram.getRulesById().entrySet().stream().filter((entry) -> !this.solvedRuleIds.contains(entry.getKey()))
				.forEach((entry) -> outputRules.add(entry.getValue()));
		InternalProgram retVal = new InternalProgram(outputRules, outputFacts);
		return retVal;
	}

	private void evaluateComponent(SCComponent comp) {
		LOGGER.debug("Evaluating component {}", comp);
		Set<InternalRule> rulesToEvaluate = this.getRulesToEvaluate(comp);
		if (rulesToEvaluate.isEmpty()) {
			LOGGER.debug("No rules to evaluate for component {}", comp);
		}
		Map<Predicate, List<Instance>> addedInstances = new HashMap<>();
		do {
			this.workingMemory.reset();
			LOGGER.debug("Starting component evaluation run...");
			for (InternalRule r : rulesToEvaluate) {
				this.evaluateRule(r);
			}
			// since we're stratified we never have to backtrack, therefore just collect the added instances
			for (IndexedInstanceStorage instanceStorage : this.workingMemory.modified()) {
				// NOTE: we're only dealing with positive instances
				addedInstances.putIfAbsent(instanceStorage.getPredicate(), new ArrayList<>());
				addedInstances.get(instanceStorage.getPredicate()).addAll(instanceStorage.getRecentlyAddedInstances());
			}
		} while (!this.workingMemory.modified().isEmpty()); // if evaluation of rules doesn't modify the working memory we have a fixed point
		LOGGER.debug("Evaluation done - reached a fixed point on component {}", comp);
		this.addFactsToProgram(addedInstances);
		LOGGER.debug("Finished adding program facts");
	}

	private Set<InternalRule> getRulesToEvaluate(SCComponent comp) {
		Set<InternalRule> retVal = new HashSet<>();
		HashSet<InternalRule> tmpPredicateRules;
		for (Node node : comp.getNodes()) {
			tmpPredicateRules = this.predicateDefiningRules.get(node.getPredicate());
			if (tmpPredicateRules != null) {
				retVal.addAll(tmpPredicateRules);
			}
		}
		return retVal;
	}

	private void evaluateRule(InternalRule rule) {
		// TODO for starting literals: only look at instances that were recently added
		// note: only applies to starting literals, negative body check needs to check everything as well
		// TODO when working on recently added instances, clear them before further modifying working memory
		LOGGER.debug("Evaluating rule {}", rule);
		RuleGroundingOrder groundingOrder = rule.getGroundingOrder();
		Collection<Literal> startingLiterals = groundingOrder.getStartingLiterals();
		Predicate tmpPred;
		IndexedInstanceStorage tmpPredInstanceStorage;
		Substitution tmpSubstitution;
		rule.getHeadAtom().getBindingVariables();
		for (Literal lit : startingLiterals) {
			tmpPred = lit.getPredicate();
			// fetch positive instances of predicate in current starting literal
			tmpPredInstanceStorage = this.workingMemory.get(tmpPred, true);
			for (Instance inst : tmpPredInstanceStorage.getAllInstances()) {
				LOGGER.debug("Instance: {}", tmpPred.getName() + inst.toString());
				tmpSubstitution = Substitution.unify(lit, inst, new Substitution());
				LOGGER.debug("Got substitution for instance: {}", tmpSubstitution);
				List<Substitution> subs = this.bindNextAtomInRule(rule, rule.getGroundingOrder().orderStartingFrom(lit), 0, tmpSubstitution, null);
				LOGGER.debug("Got {} substitutions from bindNextAtom", subs.size());
				for (Substitution subst : subs) {
					LOGGER.debug("substitution from bindNextAtom {}", subst);
					boolean canFire = this.canFire(rule, subst);
					LOGGER.debug("canFire result = {}", canFire);
					if (canFire) {
						this.fireRule(rule, subst);
					}
				}
			}
		}
	}

	private boolean canFire(InternalRule rule, Substitution substitution) {
		// positive body is completely bound
		// now check if any part of the negative body is fulfilled
		List<Atom> negativeBody = rule.getBodyAtomsNegative();
		IndexedInstanceStorage instancesForAtom;
		Atom tmpGroundAtom;
		boolean negatedAtomTrue = false;
		for (Atom a : negativeBody) {
			tmpGroundAtom = a.substitute(substitution);
			if (tmpGroundAtom.isGround()) {
				instancesForAtom = this.workingMemory.get(tmpGroundAtom, true);
				negatedAtomTrue |= instancesForAtom.containsInstance(new Instance(tmpGroundAtom.getTerms()));
			} else {
				throw Util.oops("Rule " + rule.toString() + " doesn't seem to be safe!");
			}
		}
		return !negatedAtomTrue;
	}

	private void fireRule(InternalRule rule, Substitution substitution) {
		Atom newAtom = rule.getHeadAtom().substitute(substitution);
		if (!newAtom.isGround()) {
			throw new IllegalStateException("Trying to fire rule " + rule.toString() + " with incompatible substitution " + substitution.toString());
		}
		LOGGER.debug("Firing rule - got head atom: {}", newAtom);
		if (!this.workingMemory.contains(newAtom.getPredicate())) {
			this.workingMemory.initialize(newAtom.getPredicate());
		}
		this.workingMemory.addInstance(newAtom, true);
	}

	private void addFactsToProgram(Map<Predicate, List<Instance>> instances) {
		for (Entry<Predicate, List<Instance>> entry : instances.entrySet()) {
			for (Instance inst : entry.getValue()) {
				this.additionalFacts.add(new BasicAtom(entry.getKey(), inst.terms));
			}
		}
	}

	private List<Substitution> bindNextAtomInRule(InternalRule rule, Literal[] groundingOrder, int orderPosition, Substitution partialSubstitution,
			Assignment currentAssignment) {
		if (orderPosition == groundingOrder.length) {
			return singletonList(partialSubstitution);
		}

		Literal currentLiteral = groundingOrder[orderPosition];
		Atom currentAtom = currentLiteral.getAtom();
		if (currentLiteral instanceof FixedInterpretationLiteral) {
			// Generate all substitutions for the builtin/external/interval atom.
			final List<Substitution> substitutions = ((FixedInterpretationLiteral) currentLiteral.substitute(partialSubstitution))
					.getSubstitutions(partialSubstitution);

			if (substitutions.isEmpty()) {
				return emptyList();
			}

			final List<Substitution> generatedSubstitutions = new ArrayList<>();
			for (Substitution substitution : substitutions) {
				// Continue grounding with each of the generated values.
				generatedSubstitutions.addAll(bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, substitution, currentAssignment));
			}
			return generatedSubstitutions;
		}
		if (currentAtom instanceof EnumerationAtom) {
			// Get the enumeration value and add it to the current partialSubstitution.
			((EnumerationAtom) currentAtom).addEnumerationToSubstitution(partialSubstitution);
			return bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, partialSubstitution, currentAssignment);
		}

		// check if partialVariableSubstitution already yields a ground atom
		final Atom substitute = currentAtom.substitute(partialSubstitution);

		if (substitute.isGround()) {
			// Substituted atom is ground, in case it is positive, only ground if it also holds true
			if (currentLiteral.isNegated()) {
				// Atom occurs negated in the rule, continue grounding
				return bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, partialSubstitution, currentAssignment);
			}

			if (this.stopBindingAtNonTruePositiveBody && !rule.isGround()
					&& !this.workingMemory.get(currentAtom.getPredicate(), true).containsInstance(new Instance(substitute.getTerms()))) {
				// Generate no variable substitution.
				return emptyList();
			}

			// Check if atom is also assigned true.
			final LinkedHashSet<Instance> instances = this.programFacts.get(substitute.getPredicate());
			if (!(instances == null || !instances.contains(new Instance(substitute.getTerms())))) {
				// Ground literal holds, continue finding a variable substitution.
				return bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, partialSubstitution, currentAssignment);
			}

			// Atom is not a fact already.
			final int atomId = atomStore.putIfAbsent(substitute);

			if (currentAssignment != null) {
				final ThriceTruth truth = currentAssignment.getTruth(atomId);

				if (atomId > maxAtomIdBeforeGroundingNewNoGoods || truth == null || !truth.toBoolean()) {
					// Atom currently does not hold, skip further grounding.
					// TODO: investigate grounding heuristics for use here, i.e., ground anyways to avoid re-grounding in the future.
					if (!disableInstanceRemoval) {
						removeAfterObtainingNewNoGoods.add(substitute);
						return emptyList();
					}
				}
			}
		}

		// substituted atom contains variables
		if (currentLiteral.isNegated()) {
			throw oops("Current atom should be positive at this point but is not");
		}

		IndexedInstanceStorage storage = workingMemory.get(currentAtom.getPredicate(), true);
		Collection<Instance> instances;
		if (partialSubstitution.isEmpty()) {
			if (currentLiteral.isGround()) {
				instances = singletonList(new Instance(currentLiteral.getTerms()));
			} else {
				// No variables are bound, but first atom in the body became recently true, consider all instances now.
				instances = storage.getAllInstances();
			}
		} else {
			instances = storage.getInstancesFromPartiallyGroundAtom(substitute);
		}

		ArrayList<Substitution> generatedSubstitutions = new ArrayList<>();
		for (Instance instance : instances) {
			// Check each instance if it matches with the atom.
			Substitution unified = Substitution.unify(substitute, instance, new Substitution(partialSubstitution));
			if (unified == null) {
				continue;
			}

			// Check if atom is also assigned true.
			Atom substituteClone = new BasicAtom(substitute.getPredicate(), substitute.getTerms());
			Atom substitutedAtom = substituteClone.substitute(unified);
			if (!substitutedAtom.isGround()) {
				throw oops("Grounded atom should be ground but is not");
			}

			if (this.programFacts.get(substitutedAtom.getPredicate()) == null
					|| !this.programFacts.get(substitutedAtom.getPredicate()).contains(new Instance(substitutedAtom.getTerms()))) {
				int atomId = atomStore.putIfAbsent(substitutedAtom);

				if (currentAssignment != null) {
					ThriceTruth truth = currentAssignment.getTruth(atomId);
					if (atomId > maxAtomIdBeforeGroundingNewNoGoods || truth == null || !truth.toBoolean()) {
						// Atom currently does not hold, skip further grounding.
						// TODO: investigate grounding heuristics for use here, i.e., ground anyways to avoid re-grounding in the future.
						if (!disableInstanceRemoval) {
							removeAfterObtainingNewNoGoods.add(substitutedAtom);
							continue;
						}
					}
				}
			}
			List<Substitution> boundSubstitutions = bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, unified, currentAssignment);
			generatedSubstitutions.addAll(boundSubstitutions);
		}

		return generatedSubstitutions;
	}

	private class ComponentEvaluationOrder implements Iterable<SCComponent> {

		private Map<Integer, List<SCComponent>> stratification;
		private Iterator<Entry<Integer, List<SCComponent>>> strataIterator;
		private Iterator<SCComponent> componentIterator;

		private ComponentEvaluationOrder(Map<Integer, List<SCComponent>> stratification) {
			this.stratification = stratification;
			this.strataIterator = this.stratification.entrySet().iterator();
			this.startNextStratum();
		}

		private boolean startNextStratum() {
			if (!this.strataIterator.hasNext()) {
				return false;
			}
			this.componentIterator = this.strataIterator.next().getValue().iterator();
			return true;
		}

		@Override
		public Iterator<SCComponent> iterator() {
			return new Iterator<SCComponent>() {

				@Override
				public boolean hasNext() {
					if (ComponentEvaluationOrder.this.componentIterator.hasNext()) {
						return true;
					} else {
						if (!ComponentEvaluationOrder.this.startNextStratum()) {
							return false;
						} else {
							return this.hasNext();
						}
					}
				}

				@Override
				public SCComponent next() {
					return ComponentEvaluationOrder.this.componentIterator.next();
				}

			};
		}
	}

}
