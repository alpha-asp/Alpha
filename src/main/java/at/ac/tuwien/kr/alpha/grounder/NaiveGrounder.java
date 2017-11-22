/**
 * Copyright (c) 2016-2017, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.atoms.InterpretableLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.transformation.ChoiceHeadToNormal;
import at.ac.tuwien.kr.alpha.grounder.transformation.IntervalTermToIntervalAtom;
import at.ac.tuwien.kr.alpha.grounder.transformation.VariableEqualityRemoval;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.util.Collections.emptyList;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounder extends BridgedGrounder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveGrounder.class);

	private HashMap<Predicate, ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage>> workingMemory = new HashMap<>();
	AtomStore atomStore = new AtomStore();
	private final NoGoodGenerator noGoodGenerator = new NoGoodGenerator(new IntIdGenerator(), new IntIdGenerator(), this);

	private final IntIdGenerator intIdGenerator = new IntIdGenerator();

	private HashMap<Predicate, LinkedHashSet<Instance>> factsFromProgram = new LinkedHashMap<>();
	private final ArrayList<NonGroundRule> rulesFromProgram = new ArrayList<>();
	private final HashMap<IndexedInstanceStorage, ArrayList<FirstBindingAtom>> rulesUsingPredicateWorkingMemory = new HashMap<>();

	private boolean prepareFacts = true;

	private final HashSet<Predicate> knownPredicates = new HashSet<>();
	private final HashMap<NonGroundRule, HashSet<Substitution>> knownGroundingSubstitutions = new HashMap<>();

	private Set<NonGroundRule> uniqueGroundRulePerGroundHead = new HashSet<>();
	private Map<Predicate, HashSet<NonGroundRule>> ruleHeadsToDefiningRules = new HashMap<>();

	private HashSet<IndexedInstanceStorage> modifiedWorkingMemories = new HashSet<>();

	public NaiveGrounder(Program program, Bridge... bridges) {
		this(program, p -> true, bridges);
	}

	public NaiveGrounder(Program program, java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		super(filter, bridges);
		// TODO: initialize based on program.

		// Apply program transformations/rewritings.
		applyProgramTransformations(program);

		// initialize all facts
		for (Atom fact : program.getFacts()) {
			final Predicate predicate = fact.getPredicate();

			// Record predicate
			adaptWorkingMemoryForPredicate(predicate);

			// Construct fact instance(s).
			List<Instance> instances = FactIntervalEvaluator.constructFactInstances(fact);

			// Add instances to corresponding list of facts.
			factsFromProgram.putIfAbsent(predicate, new LinkedHashSet<>());
			HashSet<Instance> internalPredicateInstances = factsFromProgram.get(predicate);
			internalPredicateInstances.addAll(instances);
		}

		// Register internal atoms.
		adaptWorkingMemoryForPredicate(RuleAtom.PREDICATE);
		adaptWorkingMemoryForPredicate(ChoiceAtom.OFF);
		adaptWorkingMemoryForPredicate(ChoiceAtom.ON);

		// Initialize rules and constraints.
		for (Rule rule : program.getRules()) {
			registerRuleOrConstraint(rule);
		}

		// FIXME: below optimisation (adding support nogoods if there is only one rule instantiation per unique atom over the predicate) could be done as a transformation (adding a non-ground constraint corresponding to the nogood that is generated by the grounder).
		// Record all unique rule heads.
		for (Map.Entry<Predicate, HashSet<NonGroundRule>> headDefiningRules : ruleHeadsToDefiningRules.entrySet()) {
			if (headDefiningRules.getValue().size() == 1) {
				NonGroundRule nonGroundRule = headDefiningRules.getValue().iterator().next();
				// Check that all variables of the body also occur in the head (otherwise grounding is not unique).
				Atom headAtom = nonGroundRule.getHeadAtom();

				// Rule is not guaranteed unique if there are facts for it.
				HashSet<Instance> potentialFacts = factsFromProgram.get(headAtom.getPredicate());
				if (potentialFacts != null && !potentialFacts.isEmpty()) {
					continue;
				}
				// Collect head and body variables.
				HashSet<VariableTerm> occurringVariablesHead = new HashSet<>(headAtom.getBindingVariables());
				HashSet<VariableTerm> occurringVariablesBody = new HashSet<>();
				for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
					occurringVariablesBody.addAll(atom.getBindingVariables());
				}
				occurringVariablesBody.removeAll(occurringVariablesHead);
				// Check if ever body variables occurs in the head.
				if (occurringVariablesBody.isEmpty()) {
					uniqueGroundRulePerGroundHead.add(nonGroundRule);
				}
			}
		}
	}

	private void applyProgramTransformations(Program program) {
		// Transform choice rules.
		new ChoiceHeadToNormal().transform(program);
		// Transform intervals.
		new IntervalTermToIntervalAtom().transform(program);
		// Remove variable equalities.
		new VariableEqualityRemoval().transform(program);
	}

	private void adaptWorkingMemoryForPredicate(Predicate predicate) {
		// Create working memory for predicate if it does not exist
		if (!workingMemory.containsKey(predicate)) {
			IndexedInstanceStorage instanceStoragePos = new IndexedInstanceStorage(predicate.getName() + "+", predicate.getArity());
			IndexedInstanceStorage instanceStorageNeg = new IndexedInstanceStorage(predicate.getName() + "-", predicate.getArity());
			// Index all positions of the storage (may impair efficiency)
			for (int i = 0; i < predicate.getArity(); i++) {
				instanceStoragePos.addIndexPosition(i);
				instanceStorageNeg.addIndexPosition(i);
			}
			workingMemory.put(predicate, new ImmutablePair<>(instanceStoragePos, instanceStorageNeg));
		}
		knownPredicates.add(predicate);
	}

	private void registerRuleOrConstraint(Rule rule) {
		// Record the rule for later use
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(intIdGenerator, rule);

		// Record defining rules for each predicate.
		if (nonGroundRule.getHeadAtom() != null) {
			Predicate headPredicate = nonGroundRule.getHeadAtom().getPredicate();
			ruleHeadsToDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
			ruleHeadsToDefiningRules.get(headPredicate).add(nonGroundRule);
		}

		// Create working memories for all predicates occurring in the rule
		for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
			// FIXME: this also contains interval/builtin predicates that are not needed.
			adaptWorkingMemoryForPredicate(predicate);
		}
		rulesFromProgram.add(nonGroundRule);

		// If the rule has fixed ground instantiations, it is not registered but grounded once like facts.
		if (nonGroundRule.groundingOrder.fixedInstantiation()) {
			return;
		}

		// Register each starting literal at the corresponding working memory.
		for (Literal literal : nonGroundRule.groundingOrder.getStartingLiterals()) {
			registerLiteralAtWorkingMemory(literal, nonGroundRule);
		}
	}

	/**
	 * Registers a starting literal of a NonGroundRule at its corresponding working memory.
	 * @param nonGroundRule   the rule in which the literal occurs.
	 */
	private void registerLiteralAtWorkingMemory(Literal literal, NonGroundRule nonGroundRule) {
		Predicate predicate = literal.getPredicate();
		if (literal.isNegated()) {
			throw new RuntimeException("Literal to register is negated. Should not happen.");
		}
		IndexedInstanceStorage workingMemory = this.workingMemory.get(predicate).getLeft();
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
	private HashMap<Integer, NoGood> prepareFactsAndNoGoodsFromGroundRules() {
		HashMap<Integer, NoGood> noGoodsFromFacts = new LinkedHashMap<>();

		for (Predicate predicate : factsFromProgram.keySet()) {
			IndexedInstanceStorage positiveStorage = workingMemory.get(predicate).getLeft();
			for (Instance instance : factsFromProgram.get(predicate)) {
				// Instead of generating NoGoods, add instance to working memories directly.
				positiveStorage.addInstance(instance);
				modifiedWorkingMemories.add(positiveStorage);
			}
		}

		for (NonGroundRule nonGroundRule : rulesFromProgram) {
			// Generate NoGoods for all rules that have a fixed grounding.
			if (!nonGroundRule.groundingOrder.fixedInstantiation()) {
				continue;
			}
			Literal[] groundingOrder = nonGroundRule.groundingOrder.getFixedGroundingOrder();
			List<Substitution> substitutions = bindNextAtomInRule(groundingOrder, 0, new Substitution(), null);
			for (Substitution substitution : substitutions) {
				createNoGoodFromSubstitution(nonGroundRule, substitution, noGoodsFromFacts);
			}
		}

		return noGoodsFromFacts;
	}


	Set<Instance> getFactsFromProgram(Predicate predicate) {
		LinkedHashSet<Instance> facts = factsFromProgram.get(predicate);
		return facts == null ? null : Collections.unmodifiableSet(facts);
	}

	boolean createsUniqueGroundHead(NonGroundRule nonGroundRule) {
		return uniqueGroundRulePerGroundHead.contains(nonGroundRule);

	}

	private void createNoGoodFromSubstitution(NonGroundRule nonGroundRule, Substitution substitution, Map<Integer, NoGood> noGoodsFromFacts) {
		if (LOGGER.isDebugEnabled()) {
			// Debugging helper: record known grounding substitutions.
			knownGroundingSubstitutions.putIfAbsent(nonGroundRule, new LinkedHashSet<>());
			knownGroundingSubstitutions.get(nonGroundRule).add(substitution);
		}
		noGoodGenerator.register(noGoodGenerator.generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution), noGoodsFromFacts);
	}

	boolean existsRuleWithPredicateInHead(Predicate predicate) {
		HashSet<NonGroundRule> definingRules = ruleHeadsToDefiningRules.get(predicate);
		return definingRules != null && !definingRules.isEmpty();
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(Assignment currentAssignment) {
		// In first call, prepare facts and ground rules.
		HashMap<Integer, NoGood> newNoGoods = new LinkedHashMap<>();
		if (prepareFacts) {
			prepareFacts = false;
			newNoGoods = prepareFactsAndNoGoodsFromGroundRules();
		}

		maxAtomIdBeforeGroundingNewNoGoods = atomStore.getHighestAtomId();
		// Compute new ground rule (evaluate joins with newly changed atoms)
		for (IndexedInstanceStorage modifiedWorkingMemory : modifiedWorkingMemories) {

			// Iterate over all rules whose body contains the predicate corresponding to the current workingMemory.
			ArrayList<FirstBindingAtom> firstBindingAtoms = rulesUsingPredicateWorkingMemory.get(modifiedWorkingMemory);
			// Skip working memories that are not used by any rule.
			if (firstBindingAtoms == null) {
				continue;
			}

			for (FirstBindingAtom firstBindingAtom : firstBindingAtoms) {
				// Use the recently added instances from the modified working memory to construct an initial substitution
				List<Substitution> substitutions = new ArrayList<>();
				NonGroundRule nonGroundRule = firstBindingAtom.rule;

				// Generate substitutions from each recent instance.
				for (Instance instance : modifiedWorkingMemory.getRecentlyAddedInstances()) {
					// Check instance if it matches with the atom.

					Substitution unified = Substitution.unify(firstBindingAtom.startingLiteral, instance, new Substitution());
					if (unified != null) {
						substitutions.addAll(bindNextAtomInRule(
							nonGroundRule.groundingOrder.orderStartingFrom(firstBindingAtom.startingLiteral),
							0,
							unified, currentAssignment));
					}
				}

				for (Substitution substitution : substitutions) {
					createNoGoodFromSubstitution(nonGroundRule, substitution, newNoGoods);
				}
			}

			// Mark instances added by updateAssignment as done
			modifiedWorkingMemory.markRecentlyAddedInstancesDone();
		}

		modifiedWorkingMemories = new LinkedHashSet<>();
		for (Atom removeAtom : removeAfterObtainingNewNoGoods) {
			final IndexedInstanceStorage storage = this.workingMemory.get(removeAtom.getPredicate()).getLeft();
			storage.removeInstance(new Instance(removeAtom.getTerms()));
		}

		removeAfterObtainingNewNoGoods = new LinkedHashSet<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Grounded NoGoods are:");
			for (Map.Entry<Integer, NoGood> noGoodEntry : newNoGoods.entrySet()) {
				LOGGER.debug("{} == {}", noGoodEntry.getValue(), noGoodToString(noGoodEntry.getValue()));
			}
			noGoodGenerator.logChoiceInformation();
		}
		return newNoGoods;
	}

	boolean disableInstanceRemoval;

	private LinkedHashSet<Atom> removeAfterObtainingNewNoGoods = new LinkedHashSet<>();
	private int maxAtomIdBeforeGroundingNewNoGoods = -1;


	private List<Substitution> bindNextAtomInRule(Literal[] groundingOrder, int orderPosition, Substitution partialSubstitution, Assignment currentAssignment) {
		if (orderPosition == groundingOrder.length) {
			return Collections.singletonList(partialSubstitution);
		}

		Literal currentAtom = groundingOrder[orderPosition];
		if (currentAtom instanceof InterpretableLiteral) {
			// Generate all substitutions for the builtin/external/interval atom.
			List<Substitution> substitutions = ((InterpretableLiteral)currentAtom).getSubstitutions(partialSubstitution);

			if (substitutions.isEmpty()) {
				return emptyList();
			}

			ArrayList<Substitution> generatedSubstitutions = new ArrayList<>();
			for (Substitution substitution : substitutions) {
				// Continue grounding with each of the generated values.
				generatedSubstitutions.addAll(bindNextAtomInRule(groundingOrder, orderPosition + 1, substitution, currentAssignment));
			}
			return generatedSubstitutions;
		}

		// check if partialVariableSubstitution already yields a ground atom
		final Atom substitute = currentAtom.substitute(partialSubstitution);

		if (substitute.isGround()) {
			// Substituted atom is ground, in case it is positive, only ground if it also holds true
			if (!currentAtom.isNegated()) {
				IndexedInstanceStorage wm = workingMemory.get(currentAtom.getPredicate()).getLeft();
				if (wm.containsInstance(new Instance(substitute.getTerms()))) {
					// Check if atom is also assigned true.
					if (factsFromProgram.get(substitute.getPredicate()) == null || !factsFromProgram.get(substitute.getPredicate()).contains(new Instance(substitute.getTerms()))) {
						// Atom is not a fact already.
						int atomId = atomStore.add(substitute);

						if (currentAssignment != null) {
							ThriceTruth truth = currentAssignment.getTruth(atomId);

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
					// Ground literal holds, continue finding a variable substitution.
					return bindNextAtomInRule(groundingOrder, orderPosition + 1, partialSubstitution, currentAssignment);
				}

				// Generate no variable substitution.
				return emptyList();
			}

			// Atom occurs negated in the rule, continue grounding
			return bindNextAtomInRule(groundingOrder, orderPosition + 1, partialSubstitution, currentAssignment);
		}

		// substituted atom contains variables
		if (currentAtom.isNegated()) {
			throw new RuntimeException("Current atom should be positive at this point but is not. Should not happen.");
		}
		ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> wms = workingMemory.get(currentAtom.getPredicate());
		IndexedInstanceStorage storage = wms.getLeft();
		Collection<Instance> instances;
		if (partialSubstitution.isEmpty()) {
			// No variables are bound, but first atom in the body became recently true, consider all instances now.
			instances = storage.getAllInstances();
		} else {
			// For selection of the instances, find ground term on which to select
			int firstGroundTermPos = 0;
			Term firstGroundTerm = null;
			for (int i = 0; i < substitute.getTerms().size(); i++) {
				Term testTerm = substitute.getTerms().get(i);
				if (testTerm.isGround()) {
					firstGroundTermPos = i;
					firstGroundTerm = testTerm;
					break;
				}
			}
			// Select matching instances
			if (firstGroundTerm != null) {
				instances = storage.getInstancesMatchingAtPosition(firstGroundTerm, firstGroundTermPos);
			} else {
				instances = new ArrayList<>(storage.getAllInstances());
			}
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
				int atomId = atomStore.add(substitutedAtom);

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
			List<Substitution> boundSubstitutions = bindNextAtomInRule(groundingOrder, orderPosition + 1, unified, currentAssignment);
			generatedSubstitutions.addAll(boundSubstitutions);
		}

		return generatedSubstitutions;
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		return noGoodGenerator.getChoiceAtoms();
	}

	@Override
	public void updateAssignment(Iterator<Assignment.Entry> it) {
		while (it.hasNext()) {
			Assignment.Entry assignment = it.next();
			Truth truthValue = assignment.getTruth();
			Atom atom = atomStore.get(assignment.getAtom());
			ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> workingMemory = this.workingMemory.get(atom.getPredicate());

			final IndexedInstanceStorage storage = truthValue.toBoolean() ? workingMemory.getLeft() : workingMemory.getRight();

			Instance instance = new Instance(atom.getTerms());

			if (!storage.containsInstance(instance)) {
				storage.addInstance(instance);
				modifiedWorkingMemories.add(storage);
			}
		}
	}

	@Override
	public void forgetAssignment(int[] atomIds) {

	}

	@Override
	public List<Integer> getUnassignedAtoms(Assignment assignment) {
		List<Integer> unassignedAtoms = new ArrayList<>();
		// Check all known atoms: assumption is that AtomStore assigned continuous values and 0 is no valid atomId.
		for (int i = 1; i <= atomStore.getHighestAtomId(); i++) {
			if (!assignment.isAssigned(i)) {
				unassignedAtoms.add(i);
			}
		}
		return unassignedAtoms;
	}

	@Override
	public int registerOutsideNoGood(NoGood noGood) {
		return noGoodGenerator.registerOutsideNoGood(noGood);
	}

	@Override
	public String atomToString(int atomId) {
		return atomStore.get(atomId).toString();
	}

	@Override
	public boolean isAtomChoicePoint(int atom) {
		return atomStore.get(atom) instanceof RuleAtom;
	}

	@Override
	public int getMaxAtomId() {
		return atomStore.getHighestAtomId();
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
			ret.append(groundAtomToString(bodyAtom, false, substitution, isFirst));
			isFirst = false;
		}
		for (Atom bodyAtom : rule.getBodyAtomsNegative()) {
			ret.append(groundAtomToString(bodyAtom, true, substitution, isFirst));
			isFirst = false;
		}
		ret.append(".");
		return ret.toString();
	}

	private static String groundAtomToString(Atom bodyAtom, boolean isNegative, Substitution substitution, boolean isFirst) {
		Atom groundBodyAtom = bodyAtom.substitute(substitution);
		return  (isFirst ? ", " : "") + (isNegative ? "not " : "") + groundBodyAtom.toString();
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
