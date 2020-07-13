package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph;
import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import at.ac.tuwien.kr.alpha.common.depgraph.Node;
import at.ac.tuwien.kr.alpha.common.depgraph.StratificationHelper;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.RuleGroundingOrders;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationLiteral;

/**
 * Evaluates the stratifiable part (if any) of the given program
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO ideally return "PartiallyEvaluatedProgram" here, grounder can use working memories created here rather than re-initialize everything
// TODO add solved rules to internal program (in extra list)
public class StratifiedEvaluation extends ProgramTransformation<AnalyzedProgram, InternalProgram> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StratifiedEvaluation.class);

	private StratificationHelper stratificationHelper = new StratificationHelper();

	private WorkingMemory workingMemory = new WorkingMemory();
	private Map<Predicate, HashSet<InternalRule>> predicateDefiningRules;
	private Map<Predicate, LinkedHashSet<Instance>> programFacts;

	private Map<Predicate, Set<Instance>> modifiedInLastEvaluationRun = new HashMap<>();

	// context settings for bindNextAtom
	private boolean stopBindingAtNonTruePositiveBody = true;

	private Set<Atom> additionalFacts = new HashSet<>();
	private Set<Integer> solvedRuleIds = new HashSet<>();

	@Override
	public InternalProgram apply(AnalyzedProgram inputProgram) {
		ComponentGraph componentGraph = inputProgram.getComponentGraph();
		Map<Integer, List<SCComponent>> strata = this.stratificationHelper.calculateStratification(componentGraph);
		this.predicateDefiningRules = inputProgram.getPredicateDefiningRules();
		this.programFacts = inputProgram.getFactsByPredicate();
		// set up list of atoms which are known to be true - we expand on this one
		Map<Predicate, Set<Instance>> knownFacts = new LinkedHashMap<>(inputProgram.getFactsByPredicate());
		for (Map.Entry<Predicate, Set<Instance>> entry : knownFacts.entrySet()) {
			this.workingMemory.initialize(entry.getKey());
			this.workingMemory.addInstances(entry.getKey(), true, entry.getValue());
		}

		for (InternalRule nonGroundRule : inputProgram.getRulesById().values()) {
			// Create working memories for all predicates occurring in the rule
			for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
				workingMemory.initialize(predicate);
			}
		}

		this.workingMemory.reset();

		ComponentEvaluationOrder evaluationOrder = new ComponentEvaluationOrder(strata);
		for (SCComponent currComponent : evaluationOrder) {
			this.evaluateComponent(currComponent);
		}

		// build the resulting program
		List<Atom> outputFacts = this.buildOutputFacts(inputProgram.getFacts(), this.additionalFacts);
		List<InternalRule> outputRules = new ArrayList<>();
		inputProgram.getRulesById().entrySet().stream().filter((entry) -> !this.solvedRuleIds.contains(entry.getKey()))
				.forEach((entry) -> outputRules.add(entry.getValue()));
		InternalProgram retVal = new InternalProgram(outputRules, outputFacts);
		return retVal;
	}

	// extra method is better visible in CPU traces when profiling
	private List<Atom> buildOutputFacts(List<Atom> initialFacts, Set<Atom> newFacts) {
		Set<Atom> atomSet = new LinkedHashSet<>(initialFacts);
		atomSet.addAll(newFacts);
		return new ArrayList<>(atomSet);
	}

	private void evaluateComponent(SCComponent comp) {
		LOGGER.debug("Evaluating component {}", comp);
		Set<InternalRule> rulesToEvaluate = this.getRulesToEvaluate(comp);
		if (rulesToEvaluate.isEmpty()) {
			LOGGER.debug("No rules to evaluate for component {}", comp);
			return;
		}
		Map<Predicate, List<Instance>> addedInstances = new HashMap<>();
		this.prepareComponentEvaluation(rulesToEvaluate);
		do {
			this.workingMemory.reset();
			LOGGER.debug("Starting component evaluation run...");
			for (InternalRule r : rulesToEvaluate) {
				this.evaluateRule(r);
			}
			this.modifiedInLastEvaluationRun = new HashMap<>();
			// since we're stratified we never have to backtrack, therefore just collect the added instances
			for (IndexedInstanceStorage instanceStorage : this.workingMemory.modified()) {
				// NOTE: we're only dealing with positive instances
				addedInstances.putIfAbsent(instanceStorage.getPredicate(), new ArrayList<>());
				addedInstances.get(instanceStorage.getPredicate()).addAll(instanceStorage.getRecentlyAddedInstances());
				this.modifiedInLastEvaluationRun.putIfAbsent(instanceStorage.getPredicate(), new LinkedHashSet<>());
				this.modifiedInLastEvaluationRun.get(instanceStorage.getPredicate()).addAll(instanceStorage.getRecentlyAddedInstances());
				instanceStorage.markRecentlyAddedInstancesDone();

			}
		} while (!this.workingMemory.modified().isEmpty()); // if evaluation of rules doesn't modify the working memory we have a fixed point
		LOGGER.debug("Evaluation done - reached a fixed point on component {}", comp);
		this.addFactsToProgram(addedInstances);
		rulesToEvaluate.forEach((rule) -> this.solvedRuleIds.add(rule.getRuleId()));
		LOGGER.debug("Finished adding program facts");
	}

	/**
	 * To be called at the start of evaluateComponent. Adds all known instances of the predicates occurring in the given set
	 * of rules to the
	 * "modifiedInLastEvaluationRun" map in order to "bootstrap" incremental grounding, i.e. making sure that those
	 * instances are taken into account for ground
	 * substitutions by evaluateRule.
	 */
	private void prepareComponentEvaluation(Set<InternalRule> rulesToEvaluate) {
		this.modifiedInLastEvaluationRun = new HashMap<>();
		Predicate tmpPredicate;
		IndexedInstanceStorage tmpInstances;
		for (InternalRule rule : rulesToEvaluate) {
			// register rule head instances
			tmpPredicate = rule.getHeadAtom().getPredicate();
			tmpInstances = this.workingMemory.get(tmpPredicate, true);
			this.modifiedInLastEvaluationRun.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
			if (tmpInstances != null) {
				this.modifiedInLastEvaluationRun.get(tmpPredicate).addAll(tmpInstances.getAllInstances());
			}
			// register positive body instances
			for (Atom a : rule.getBodyAtomsPositive()) {
				tmpPredicate = a.getPredicate();
				tmpInstances = this.workingMemory.get(tmpPredicate, true);
				this.modifiedInLastEvaluationRun.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
				if (tmpInstances != null) {
					this.modifiedInLastEvaluationRun.get(tmpPredicate).addAll(tmpInstances.getAllInstances());
				}
			}
		}
	}

	private void evaluateRule(InternalRule rule) {
		LOGGER.debug("Evaluating rule {}", rule);
		List<Substitution> satisfyingSubstitutions = this.calculateSatisfyingSubstitutionsForRule(rule);
		for (Substitution subst : satisfyingSubstitutions) {
			this.fireRule(rule, subst);
		}
	}

	private List<Substitution> calculateSatisfyingSubstitutionsForRule(InternalRule rule) {
		LOGGER.debug("Grounding rule {}", rule);
		RuleGroundingOrders groundingOrders = rule.getGroundingOrders();
		List<Substitution> groundSubstitutions = new ArrayList<>(); // the actual full ground substitutions for the rule
		LOGGER.debug("Is fixed rule? {}", rule.getGroundingOrders().fixedInstantiation());
		if (groundingOrders.fixedInstantiation()) {
			groundSubstitutions.addAll(this.bindNextAtomInRule(rule, groundingOrders.getFixedGroundingOrder(), 0, new Substitution()));
		} else {
			Collection<Literal> startingLiterals = groundingOrders.getStartingLiterals();
			for (Literal lit : startingLiterals) {
				groundSubstitutions.addAll(this.calcSubstitutionsForStartingLiteral(rule, lit));
			}
		}
		return groundSubstitutions;
	}

	private List<Substitution> calcSubstitutionsForStartingLiteral(InternalRule rule, Literal startingLiteral) {
		List<Substitution> retVal = new ArrayList<>();
		Substitution partialStartSubstitution = new Substitution();
		if (startingLiteral instanceof FixedInterpretationLiteral) {
			// if the starting literal is a builtin or external, we don't have instances in working memory,
			// but just get fixed substitutions and let bindNextAtomInRule do it's magic ;)
			FixedInterpretationLiteral fixedInterpretationLiteral = (FixedInterpretationLiteral) startingLiteral;
			for (Substitution fixedSubstitution : fixedInterpretationLiteral.getSatisfyingSubstitutions(partialStartSubstitution)) {
				LOGGER.debug("calling bindNextAtom, startingLiteral = {}", startingLiteral);
				retVal.addAll(this.bindNextAtomInRule(rule, rule.getGroundingOrders().orderStartingFrom(startingLiteral), 0, fixedSubstitution));
			}
		} else {
			Predicate predicate = startingLiteral.getPredicate();
			if (!this.modifiedInLastEvaluationRun.containsKey(predicate)) {
				return retVal;
			}
			Set<Instance> instances;
			if (!this.workingMemory.contains(predicate)) {
				LOGGER.debug("No instances for starting literal " + predicate.toString() + " in working memory!");
			} else {
				instances = this.modifiedInLastEvaluationRun.get(predicate);
				for (Instance inst : instances) {
					LOGGER.trace("Building substitutions for instance: {}", predicate.getName() + inst.toString());
					partialStartSubstitution = Substitution.unify(startingLiteral, inst, new Substitution());
					LOGGER.trace("Got partial substitution for instance: {}", partialStartSubstitution);
					if (partialStartSubstitution == null) {
						// continue at this point - when substitution is null it means there's no valid unifier, therefore move on
						continue;
					}
					LOGGER.trace("calling bindNextAtom, startingLiteral = {}", startingLiteral);
					List<Substitution> subs = this.bindNextAtomInRule(rule, rule.getGroundingOrders().orderStartingFrom(startingLiteral), 0,
							partialStartSubstitution);
					retVal.addAll(subs);
				}
			}
		}
		return retVal;
	}

	private boolean canFire(InternalRule rule, Substitution substitution) {
		List<Literal> body = rule.getBody();
		boolean bodyFulfilled = true;

		for (Literal lit : body) {
			bodyFulfilled &= this.isLiteralTrue(lit, substitution);
		}
		return bodyFulfilled;
	}

	private void fireRule(InternalRule rule, Substitution substitution) {
		Atom newAtom = rule.getHeadAtom().substitute(substitution);
		if (!newAtom.isGround()) {
			throw new IllegalStateException("Trying to fire rule " + rule.toString() + " with incompatible substitution " + substitution.toString());
		}
		LOGGER.debug("Firing rule - got head atom: {}", newAtom);
		this.workingMemory.addInstance(newAtom, true);
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

	private void addFactsToProgram(Map<Predicate, List<Instance>> instances) {
		for (Entry<Predicate, List<Instance>> entry : instances.entrySet()) {
			for (Instance inst : entry.getValue()) {
				this.additionalFacts.add(new BasicAtom(entry.getKey(), inst.terms));
			}
		}
	}

	private boolean isLiteralTrue(Literal lit, Substitution substitution) {
		if (lit instanceof FixedInterpretationLiteral) {
			return this.isFixedInterpretationLiteralTrue((FixedInterpretationLiteral) lit.substitute(substitution), substitution);
		}
		if (lit instanceof EnumerationLiteral) {
			return this.isEnumerationLiteralTrue((EnumerationLiteral) lit, substitution);
		}
		Literal groundLiteral = lit.substitute(substitution);
		if (!groundLiteral.isGround()) {
			throw Util.oops("Literal (" + groundLiteral.getClass().getSimpleName() + ")" + groundLiteral.toString() + " should be ground, but is not");
		}
		Atom at = groundLiteral.getAtom();
		IndexedInstanceStorage positiveInstances = this.workingMemory.get(at, true);
		boolean instanceKnown = positiveInstances.containsInstance(new Instance(at.getTerms()));
		return lit.isNegated() ? (!instanceKnown) : instanceKnown;
	}

	private boolean isFixedInterpretationLiteralTrue(FixedInterpretationLiteral lit, Substitution substitution) {
		if (lit instanceof ExternalLiteral) {
			return this.isExternalLiteralTrue((ExternalLiteral) lit, substitution);
		}
		Set<VariableTerm> variables = lit.getOccurringVariables();
		Substitution candidate = new Substitution(substitution);
		List<Substitution> validSubstitutions = lit.getSatisfyingSubstitutions(substitution);
		if (validSubstitutions.isEmpty()) {
			// even if lit is ground, an empty substitution must be here for the whole substitution to be valid
			return false;
		}
		Term candidateSubstitute;
		Term validSubstitute;
		boolean candidateValid = true;
		for (Substitution valid : validSubstitutions) {
			candidateValid = true;
			for (VariableTerm var : variables) {
				candidateSubstitute = candidate.eval(var);
				validSubstitute = valid.eval(var);
				candidateValid &= candidateSubstitute.equals(validSubstitute);
			}
			if (candidateValid) {
				// if the given substitution matches one of the valid substitutions for the literal,
				// the literal is true
				return true;
			}
		}
		// at this point, we checked all valid substitutions, but found none that matches the candidate
		// --> literal is false
		return false;
	}

	private boolean isExternalLiteralTrue(ExternalLiteral lit, Substitution substitution) {
		ExternalAtom at = lit.getAtom();
		Substitution resultSubstitution = new Substitution(); // we use this substitution to check for binding of output terms
		for (VariableTerm var : substitution.getOccurringVariables()) {
			if (!at.getOutput().contains(var)) { // add every binding other than output of the external to resultSubstitution
				resultSubstitution.put(var, substitution.eval(var));
			}
		}
		List<Substitution> validSubstitutions = lit.getSatisfyingSubstitutions(resultSubstitution);
		if (validSubstitutions.isEmpty()) {
			// even if lit is ground, an empty substitution must be here for the whole substitution to be valid
			return false;
		}
		// now check that substitution matches one of the valid substitutions given by the external literal
		Term candidateSubstitute;
		Term validSubstitute;
		boolean candidateValid;
		for (Substitution valid : validSubstitutions) {
			candidateValid = true;
			for (VariableTerm var : lit.getOccurringVariables()) {
				candidateSubstitute = substitution.eval(var);
				validSubstitute = valid.eval(var);
				candidateValid &= candidateSubstitute.equals(validSubstitute);
			}
			if (candidateValid) {
				// if the given substitution matches one of the valid substitutions for the literal,
				// the literal is true
				return true;
			}
		}
		// at this point, we checked all valid substitutions, but found none that matches the candidate
		// --> literal is false
		return false;
	}

	private boolean isEnumerationLiteralTrue(EnumerationLiteral lit, Substitution subst) {
//		return EnumerationAtom.isTrueUnderSubstitution(lit.getAtom(), subst);
		return false;
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
					if (ComponentEvaluationOrder.this.componentIterator == null) {
						// can happen when there are actually no components, as is the case for empty programs or programs just consisting of
						// facts
						return false;
					}
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
