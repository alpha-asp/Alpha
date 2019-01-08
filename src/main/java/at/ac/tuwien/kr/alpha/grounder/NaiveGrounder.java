/**
 * Copyright (c) 2016-2019, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.heuristics.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.grounder.structure.AnalyzeUnjustified;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;
import at.ac.tuwien.kr.alpha.grounder.transformation.*;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016-2019, the Alpha Team.
 */
public class NaiveGrounder extends BridgedGrounder implements ProgramAnalyzingGrounder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveGrounder.class);

	private final WorkingMemory workingMemory = new WorkingMemory();
	private final AtomStore atomStore;
	private final NogoodRegistry registry = new NogoodRegistry();
	final NoGoodGenerator noGoodGenerator;
	private final ChoiceRecorder choiceRecorder;
	private final ProgramAnalysis programAnalysis;
	private final AnalyzeUnjustified analyzeUnjustified;

	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram = new LinkedHashMap<>();
	private final Map<IndexedInstanceStorage, ArrayList<FirstBindingAtom>> rulesUsingPredicateWorkingMemory = new HashMap<>();
	private final Map<NonGroundRule, HashSet<Substitution>> knownGroundingSubstitutions = new HashMap<>();
	private final Map<Integer, NonGroundRule> knownNonGroundRules = new HashMap<>();

	private ArrayList<NonGroundRule> fixedRules = new ArrayList<>();
	private LinkedHashSet<Atom> removeAfterObtainingNewNoGoods = new LinkedHashSet<>();
	private int maxAtomIdBeforeGroundingNewNoGoods = -1;
	private boolean disableInstanceRemoval;
	private boolean useCountingGridNormalization;
	
	private GrounderHeuristicsConfiguration heuristicsConfiguration = new GrounderHeuristicsConfiguration();	// TODO: make configurable from CLI
	
	/**
	 * If this configuration parameter is {@code true} (which it is by default),
	 * the grounder stops grounding a rule if it contains a positive body atom which is not
	 * yet true, except if the whole rule is already ground. Is currently used only internally,
	 * but might be used for grounder heuristics and also set from the outside in the future.
	 */
	private boolean stopBindingAtNonTruePositiveBody = true;

	public NaiveGrounder(Program program, AtomStore atomStore, Bridge... bridges) {
		this(program, atomStore, p -> true, false, bridges);
	}

	NaiveGrounder(Program program, AtomStore atomStore, java.util.function.Predicate<Predicate> filter, boolean useCountingGrid, Bridge... bridges) {
		super(filter, bridges);
		this.atomStore = atomStore;

		programAnalysis = new ProgramAnalysis(program);
		analyzeUnjustified = new AnalyzeUnjustified(programAnalysis, atomStore, factsFromProgram);

		// Apply program transformations/rewritings.
		useCountingGridNormalization = useCountingGrid;
		applyProgramTransformations(program);
		LOGGER.debug("Transformed input program is:\n" + program);

		initializeFactsAndRules(program);

		final Set<NonGroundRule> uniqueGroundRulePerGroundHead = getRulesWithUniqueHead();
		choiceRecorder = new ChoiceRecorder(atomStore);
		noGoodGenerator = new NoGoodGenerator(atomStore, choiceRecorder, factsFromProgram, programAnalysis, uniqueGroundRulePerGroundHead);
	}

	private void initializeFactsAndRules(Program program) {
		// initialize all facts
		for (Atom fact : program.getFacts()) {
			final Predicate predicate = fact.getPredicate();

			// Record predicate
			workingMemory.initialize(predicate);

			// Construct fact instance(s).
			List<Instance> instances = FactIntervalEvaluator.constructFactInstances(fact);

			// Add instances to corresponding list of facts.
			factsFromProgram.putIfAbsent(predicate, new LinkedHashSet<>());
			HashSet<Instance> internalPredicateInstances = factsFromProgram.get(predicate);
			internalPredicateInstances.addAll(instances);
		}

		// Register internal atoms.
		workingMemory.initialize(RuleAtom.PREDICATE);
		workingMemory.initialize(ChoiceAtom.OFF);
		workingMemory.initialize(ChoiceAtom.ON);

		// Initialize rules and constraints.
		for (Rule rule : program.getRules()) {
			// Record the rule for later use
			NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
			knownNonGroundRules.put(nonGroundRule.getRuleId(), nonGroundRule);

			// Record defining rules for each predicate.
			if (nonGroundRule.getHeadAtom() != null) {
				Predicate headPredicate = nonGroundRule.getHeadAtom().getPredicate();
				programAnalysis.recordDefiningRule(headPredicate, nonGroundRule);
			}

			// Create working memories for all predicates occurring in the rule
			for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
				// FIXME: this also contains interval/builtin predicates that are not needed.
				workingMemory.initialize(predicate);
			}

			// If the rule has fixed ground instantiations, it is not registered but grounded once like facts.
			if (nonGroundRule.groundingOrder.fixedInstantiation()) {
				fixedRules.add(nonGroundRule);
				continue;
			}

			// Register each starting literal at the corresponding working memory.
			for (Literal literal : nonGroundRule.groundingOrder.getStartingLiterals()) {
				registerLiteralAtWorkingMemory(literal, nonGroundRule);
			}
		}
	}

	private Set<NonGroundRule> getRulesWithUniqueHead() {
		// FIXME: below optimisation (adding support nogoods if there is only one rule instantiation per unique atom over the interpretation) could be done as a transformation (adding a non-ground constraint corresponding to the nogood that is generated by the grounder).
		// Record all unique rule heads.
		final Set<NonGroundRule> uniqueGroundRulePerGroundHead = new HashSet<>();

		for (Map.Entry<Predicate, HashSet<NonGroundRule>> headDefiningRules : programAnalysis.getPredicateDefiningRules().entrySet()) {
			if (headDefiningRules.getValue().size() != 1) {
				continue;
			}

			NonGroundRule nonGroundRule = headDefiningRules.getValue().iterator().next();
			// Check that all variables of the body also occur in the head (otherwise grounding is not unique).
			Atom headAtom = nonGroundRule.getHeadAtom();

			// Rule is not guaranteed unique if there are facts for it.
			HashSet<Instance> potentialFacts = factsFromProgram.get(headAtom.getPredicate());
			if (potentialFacts != null && !potentialFacts.isEmpty()) {
				continue;
			}

			// Collect head and body variables.
			HashSet<VariableTerm> occurringVariablesHead = new HashSet<>(headAtom.toLiteral().getBindingVariables());
			HashSet<VariableTerm> occurringVariablesBody = new HashSet<>();
			for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
				occurringVariablesBody.addAll(atom.toLiteral().getBindingVariables());
			}
			occurringVariablesBody.removeAll(occurringVariablesHead);

			// Check if ever body variables occurs in the head.
			if (occurringVariablesBody.isEmpty()) {
				uniqueGroundRulePerGroundHead.add(nonGroundRule);
			}
		}
		return uniqueGroundRulePerGroundHead;
	}

	private void applyProgramTransformations(Program program) {
		// Transform choice rules.
		new ChoiceHeadToNormal().transform(program);
		// Transform cardinality aggregates.
		new CardinalityNormalization(!useCountingGridNormalization).transform(program);
		// Transform sum aggregates.
		new SumNormalization().transform(program);
		// Transform intervals.
		new IntervalTermToIntervalAtom().transform(program);
		// Remove variable equalities.
		new VariableEqualityRemoval().transform(program);
		// Transform enumeration atoms.
		new EnumerationRewriting().transform(program);
		EnumerationAtom.resetEnumerations();
	}

	/**
	 * Registers a starting literal of a NonGroundRule at its corresponding working memory.
	 * @param nonGroundRule   the rule in which the literal occurs.
	 */
	private void registerLiteralAtWorkingMemory(Literal literal, NonGroundRule nonGroundRule) {
		if (literal.isNegated()) {
			throw new RuntimeException("Literal to register is negated. Should not happen.");
		}
		IndexedInstanceStorage workingMemory = this.workingMemory.get(literal.getPredicate(), true);
		rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemory, new ArrayList<>());
		rulesUsingPredicateWorkingMemory.get(workingMemory).add(new FirstBindingAtom(nonGroundRule, literal));
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		Map<Predicate, SortedSet<Atom>> predicateInstances = new LinkedHashMap<>();
		SortedSet<Predicate> knownPredicates = new TreeSet<>();

		// Iterate over all true atomIds, computeNextAnswerSet instances from atomStore and add them if not filtered.
		for (int trueAtom : trueAtoms) {
			final Atom atom = atomStore.get(trueAtom);
			Predicate predicate = atom.getPredicate();

			// Skip atoms over internal predicates.
			if (predicate.isInternal()) {
				continue;
			}

			// Skip filtered predicates.
			if (!filter.test(predicate)) {
				continue;
			}

			knownPredicates.add(predicate);
			predicateInstances.putIfAbsent(predicate, new TreeSet<>());
			Set<Atom> instances = predicateInstances.get(predicate);
			instances.add(atom);
		}

		// Add true atoms from facts.
		for (Map.Entry<Predicate, LinkedHashSet<Instance>> facts : factsFromProgram.entrySet()) {
			Predicate factPredicate = facts.getKey();
			// Skip atoms over internal predicates.
			if (factPredicate.isInternal()) {
				continue;
			}
			// Skip filtered predicates.
			if (!filter.test(factPredicate)) {
				continue;
			}
			// Skip predicates without any instances.
			if (facts.getValue().isEmpty()) {
				continue;
			}
			knownPredicates.add(factPredicate);
			predicateInstances.putIfAbsent(factPredicate, new TreeSet<>());
			for (Instance factInstance : facts.getValue()) {
				SortedSet<Atom> instances = predicateInstances.get(factPredicate);
				instances.add(new BasicAtom(factPredicate, factInstance.terms));
			}
		}

		if (knownPredicates.isEmpty()) {
			return BasicAnswerSet.EMPTY;
		}

		return new BasicAnswerSet(knownPredicates, predicateInstances);
	}

	/**
	 * Prepares facts of the input program for joining and derives all NoGoods representing ground rules. May only be called once.
	 * @return
	 */
	private HashMap<Integer, NoGood> bootstrap() {
		final HashMap<Integer, NoGood> groundNogoods = new LinkedHashMap<>();

		for (Predicate predicate : factsFromProgram.keySet()) {
			// Instead of generating NoGoods, add instance to working memories directly.
			workingMemory.addInstances(predicate, true, factsFromProgram.get(predicate));
		}

		for (NonGroundRule nonGroundRule : fixedRules) {
			// Generate NoGoods for all rules that have a fixed grounding.
			RuleGroundingOrder groundingOrder = nonGroundRule.groundingOrder.getFixedGroundingOrder();
			List<Substitution> substitutions = bindNextAtomInRule(nonGroundRule, groundingOrder, 0, new Substitution(), null);
			for (Substitution substitution : substitutions) {
				registry.register(noGoodGenerator.generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution), groundNogoods);
			}
		}

		fixedRules = null;

		return groundNogoods;
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(Assignment currentAssignment) {
		// In first call, prepare facts and ground rules.
		final Map<Integer, NoGood> newNoGoods = fixedRules != null ? bootstrap() : new LinkedHashMap<>();

		maxAtomIdBeforeGroundingNewNoGoods = atomStore.getMaxAtomId();
		// Compute new ground rule (evaluate joins with newly changed atoms)
		for (IndexedInstanceStorage modifiedWorkingMemory : workingMemory.modified()) {
			// Skip predicates solely used in the solver which do not occur in rules.
			Predicate workingMemoryPredicate = modifiedWorkingMemory.getPredicate();
			if (workingMemoryPredicate.isSolverInternal()) {
				continue;
			}

			// Iterate over all rules whose body contains the interpretation corresponding to the current workingMemory.
			final ArrayList<FirstBindingAtom> firstBindingAtoms = rulesUsingPredicateWorkingMemory.get(modifiedWorkingMemory);

			// Skip working memories that are not used by any rule.
			if (firstBindingAtoms == null) {
				continue;
			}

			for (FirstBindingAtom firstBindingAtom : firstBindingAtoms) {
				// Use the recently added instances from the modified working memory to construct an initial substitution
				NonGroundRule nonGroundRule = firstBindingAtom.rule;

				// Generate substitutions from each recent instance.
				for (Instance instance : modifiedWorkingMemory.getRecentlyAddedInstances()) {
					// Check instance if it matches with the atom.

					final Substitution unifier = Substitution.unify(firstBindingAtom.startingLiteral, instance, new Substitution());

					if (unifier == null) {
						continue;
					}

					final List<Substitution> substitutions = bindNextAtomInRule(
						nonGroundRule,
						nonGroundRule.groundingOrder.orderStartingFrom(firstBindingAtom.startingLiteral),
						0,
						unifier,
						currentAssignment
					);

					for (Substitution substitution : substitutions) {
						registry.register(noGoodGenerator.generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution), newNoGoods);
					}
				}
			}

			// Mark instances added by updateAssignment as done
			modifiedWorkingMemory.markRecentlyAddedInstancesDone();
		}

		workingMemory.reset();
		for (Atom removeAtom : removeAfterObtainingNewNoGoods) {
			final IndexedInstanceStorage storage = this.workingMemory.get(removeAtom, true);
			storage.removeInstance(new Instance(removeAtom.getTerms()));
		}

		removeAfterObtainingNewNoGoods = new LinkedHashSet<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Grounded NoGoods are:");
			for (Map.Entry<Integer, NoGood> noGoodEntry : newNoGoods.entrySet()) {
				LOGGER.debug("{} == {}", noGoodEntry.getValue(), atomStore.noGoodToString(noGoodEntry.getValue()));
			}
			LOGGER.debug("{}", choiceRecorder);
		}
		return newNoGoods;
	}

	@Override
	public int register(NoGood noGood) {
		return registry.register(noGood);
	}

	private List<Substitution> bindNextAtomInRule(NonGroundRule rule, RuleGroundingOrder groundingOrder, int orderPosition, Substitution partialSubstitution, Assignment currentAssignment) {
		boolean laxGrounderHeuristic = true; // TODO
		
		Literal[] literals = groundingOrder.getOtherLiterals(); // can contain positive and negative literals
		if (orderPosition == literals.length) {
			return singletonList(partialSubstitution);
		}
		
//		if (orderPosition >= groundingOrder.getPositionFromWhichAllVarsAreBound()) {
			// TODO: now all vars are bound and we have to decide whether to continue binding or to terminate
			// TODO: use parameters in heuristicsConfiguration to make this decision (maybe split literals into positive and negative to make it easier?)
//		}

		Literal currentLiteral = literals[orderPosition];
		Atom currentAtom = currentLiteral.getAtom();
		if (currentLiteral instanceof FixedInterpretationLiteral) {
			// Generate all substitutions for the builtin/external/interval atom.
			final List<Substitution> substitutions = ((FixedInterpretationLiteral)currentLiteral.substitute(partialSubstitution)).getSubstitutions(partialSubstitution);

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

			if (stopBindingAtNonTruePositiveBody && !rule.getRule().isGround()
					&& !workingMemory.get(currentAtom.getPredicate(), true).containsInstance(new Instance(substitute.getTerms()))) {
				if (laxGrounderHeuristic) {
					LOGGER.debug("Not aborting binding of rule " + rule + " because lax grounder heuristic is active");
				} else {
					// Generate no variable substitution.
					return emptyList();
				}
			}

			// Check if atom is also assigned true.
			final LinkedHashSet<Instance> instances = factsFromProgram.get(substitute.getPredicate());
			if (!(instances == null || !instances.contains(new Instance(substitute.getTerms())))) {
				// Ground literal holds, continue finding a variable substitution.
				return bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, partialSubstitution, currentAssignment);
			}

			// Atom is not a fact already.
			if (storeAtomAndTerminateIfAtomDoesNotHold(currentAssignment, laxGrounderHeuristic, substitute)) {
				return emptyList();
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
		
		if (instances.isEmpty() && laxGrounderHeuristic && substitute.isGround()) {
			// note: this is necessary in the case that the current atom has just been grounded and is not known by the working memory yet
			// we do not add the atom to the working memory so as not to trigger additional grounding
			// (but maybe the working memory will be redesigned in the future)
			instances = singletonList(new Instance(substitute.getTerms()));
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

			if (factsFromProgram.get(substitutedAtom.getPredicate()) == null || !factsFromProgram.get(substitutedAtom.getPredicate()).contains(new Instance(substitutedAtom.getTerms()))) {
				if (storeAtomAndTerminateIfAtomDoesNotHold(currentAssignment, laxGrounderHeuristic, substitutedAtom)) {
					continue;
				}
			}
			List<Substitution> boundSubstitutions = bindNextAtomInRule(rule, groundingOrder, orderPosition + 1, unified, currentAssignment);
			generatedSubstitutions.addAll(boundSubstitutions);
		}

		return generatedSubstitutions;
	}

	private boolean storeAtomAndTerminateIfAtomDoesNotHold(Assignment currentAssignment, boolean laxGrounderHeuristic, final Atom substitute) {
		final int atomId = atomStore.putIfAbsent(substitute);

		if (currentAssignment != null) {
			if (atomId <= maxAtomIdBeforeGroundingNewNoGoods) {
				// atom has not just been grounded
				final ThriceTruth truth = currentAssignment.getTruth(atomId);

				if (truth == null || !truth.toBoolean()) {
					// Atom currently does not hold, skip further grounding.
					// TODO: investigate grounding heuristics for use here, i.e., ground anyways to avoid re-grounding in the future.
					if (!disableInstanceRemoval && !laxGrounderHeuristic) {
						// we terminate binding if positive body literal is already assigned false, even in lax grounder heuristic
						removeAfterObtainingNewNoGoods.add(substitute);
						// TODO: terminate here if atom (i.e. positive body literal) is already assigned false
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		return choiceRecorder.getAndReset();
	}

	@Override
	public void updateAssignment(Iterator<Integer> it) {
		while (it.hasNext()) {
			workingMemory.addInstance(atomStore.get(it.next()), true);
		}
	}

	@Override
	public void forgetAssignment(int[] atomIds) {
		throw new UnsupportedOperationException("Forgetting assignments is not implemented");
	}

	public void printCurrentlyKnownGroundRules() {
		System.out.println("Printing known ground rules:");
		for (Map.Entry<NonGroundRule, HashSet<Substitution>> ruleSubstitutionsEntry : knownGroundingSubstitutions.entrySet()) {
			NonGroundRule nonGroundRule = ruleSubstitutionsEntry.getKey();
			HashSet<Substitution> substitutions = ruleSubstitutionsEntry.getValue();
			for (Substitution substitution : substitutions) {
				System.out.println(groundAndPrintRule(nonGroundRule, substitution));
			}
		}
	}

	public static String groundAndPrintRule(NonGroundRule rule, Substitution substitution) {
		StringBuilder ret = new StringBuilder();
		if (!rule.isConstraint()) {
			Atom groundHead = rule.getHeadAtom().substitute(substitution);
			ret.append(groundHead.toString());
		}
		ret.append(" :- ");
		boolean isFirst = true;
		for (Atom bodyAtom : rule.getBodyAtomsPositive()) {
			ret.append(groundLiteralToString(bodyAtom.toLiteral(), substitution, isFirst));
			isFirst = false;
		}
		for (Atom bodyAtom : rule.getBodyAtomsNegative()) {
			ret.append(groundLiteralToString(bodyAtom.toLiteral(false), substitution, isFirst));
			isFirst = false;
		}
		ret.append(".");
		return ret.toString();
	}

	static String groundLiteralToString(Literal literal, Substitution substitution, boolean isFirst) {
		Literal groundLiteral = literal.substitute(substitution);
		return  (isFirst ? "" : ", ") + groundLiteral.toString();
	}

	@Override
	public NonGroundRule getNonGroundRule(Integer ruleId) {
		return knownNonGroundRules.get(ruleId);
	}

	@Override
	public boolean isFact(Atom atom) {
		LinkedHashSet<Instance> instances = factsFromProgram.get(atom.getPredicate());
		if (instances == null) {
			return false;
		}
		return instances.contains(new Instance(atom.getTerms()));
	}

	@Override
	public Set<Literal> justifyAtom(int atomToJustify, Assignment currentAssignment) {
		return analyzeUnjustified.analyze(atomToJustify, currentAssignment);
	}

	private static class FirstBindingAtom {
		NonGroundRule rule;
		Literal startingLiteral;

		FirstBindingAtom(NonGroundRule rule, Literal startingLiteral) {
			this.rule = rule;
			this.startingLiteral = startingLiteral;
		}
	}
}
