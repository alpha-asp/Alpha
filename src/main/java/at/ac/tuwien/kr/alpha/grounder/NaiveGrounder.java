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
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.symbols.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Variable;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.IntervalAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.transformation.ChoiceHeadToNormal;
import at.ac.tuwien.kr.alpha.grounder.transformation.IntervalTermToIntervalAtom;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounder extends BridgedGrounder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveGrounder.class);

	private WorkingMemory workingMemory = new WorkingMemory();
	private AtomStore atomStore = new AtomStore();

	private NogoodRegistry registry = new NogoodRegistry();

	private final IntIdGenerator choiceAtomsGenerator = new IntIdGenerator();

	private HashMap<Predicate, LinkedHashSet<Instance>> factsFromProgram = new LinkedHashMap<>();
	private final ArrayList<NonGroundRule> rulesFromProgram = new ArrayList<>();
	private final HashMap<IndexedInstanceStorage, ArrayList<FirstBindingAtom>> rulesUsingPredicateWorkingMemory = new HashMap<>();

	private boolean prepareFacts = true;

	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
	private final HashMap<NonGroundRule, HashSet<Substitution>> knownGroundingSubstitutions = new HashMap<>();

	private Set<NonGroundRule> uniqueGroundRulePerGroundHead = new HashSet<>();
	private Map<Predicate, HashSet<NonGroundRule>> ruleHeadsToDefiningRules = new HashMap<>();

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

			// Record interpretation
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
			registerRuleOrConstraint(rule);
		}

		// FIXME: below optimisation (adding support nogoods if there is only one rule instantiation per unique atom over the interpretation) could be done as a transformation (adding a non-ground constraint corresponding to the nogood that is generated by the grounder).
		// Record all unique rule heads.
		for (Map.Entry<Predicate, HashSet<NonGroundRule>> headDefiningRules : ruleHeadsToDefiningRules.entrySet()) {
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
			List<Variable> occurringVariablesHead = headAtom.getBindingVariables();

			boolean found = false;
			for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
				for (Variable variableTerm : atom.getBindingVariables()) {
					if (!occurringVariablesHead.contains(variableTerm)) {
						found = true;
						break;
					}
				}

				if (found) {
					break;
				}
			}

			// Check if ever body variables occurs in the head.
			if (!found) {
				uniqueGroundRulePerGroundHead.add(nonGroundRule);
			}
		}
	}

	private void applyProgramTransformations(Program program) {
		// Transform choice rules.
		new ChoiceHeadToNormal().transform(program);
		// Transform intervals.
		new IntervalTermToIntervalAtom().transform(program);
	}

	private void registerRuleOrConstraint(Rule rule) {
		// Record the rule for later use
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);

		// Record defining rules for each interpretation.
		if (nonGroundRule.getHeadAtom() != null) {
			Predicate headPredicate = nonGroundRule.getHeadAtom().getPredicate();
			ruleHeadsToDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
			ruleHeadsToDefiningRules.get(headPredicate).add(nonGroundRule);
		}

		// Create working memories for all predicates occurring in the rule
		for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
			// FIXME: this also contains interval/builtin predicates that are not needed.
			workingMemory.initialize(predicate);
		}
		rulesFromProgram.add(nonGroundRule);

		// Register the rule at the working memory corresponding to its first interpretation.
		Predicate firstBodyPredicate = nonGroundRule.usedFirstBodyPredicate();
		if (firstBodyPredicate == null) {
			// No ordinary first body interpretation, hence it only contains ground builtin predicates.
			return;
		}

		// Register each atom occurring in the body of the rule at its corresponding working memory.
		HashSet<Atom> registeredPositiveAtoms = new HashSet<>();
		for (int i = 0; i < nonGroundRule.getBodyAtomsPositive().size(); i++) {
			registerAtomAtWorkingMemory(nonGroundRule, registeredPositiveAtoms, i);
		}

		// Register negative literals only if the rule contains no positive literals (necessary grounding is ensured by safety of rules).
		if (nonGroundRule.getBodyAtomsPositive().isEmpty()) {
			HashSet<Atom> registeredNegativeAtoms = new HashSet<>();
			for (int i = 0; i < nonGroundRule.getBodyAtomsNegative().size(); i++) {
				registerAtomAtWorkingMemory(nonGroundRule, registeredNegativeAtoms, i);
			}
		}
	}

	/**
	 * Registers an atom occurring in a rule at its corresponding working memory if it has not already been treated this way.
	 * @param nonGroundRule the rule into which the atom occurs.
	 * @param registeredAtoms a set of already registered atoms (will skip if the interpretation of the current atom occurs in this set). This set will be extended by the current atom.
	 * @param atomPos the position in the rule of the atom.
	 */
	private void registerAtomAtWorkingMemory(NonGroundRule nonGroundRule, HashSet<Atom> registeredAtoms, int atomPos) {
		Literal bodyAtom = nonGroundRule.getBodyAtom(atomPos);
		if (registeredAtoms.contains(bodyAtom)) {
			return;
		}

		registeredAtoms.add(bodyAtom);
		IndexedInstanceStorage storage = this.workingMemory.get(bodyAtom);
		rulesUsingPredicateWorkingMemory.putIfAbsent(storage, new ArrayList<>());
		rulesUsingPredicateWorkingMemory.get(storage).add(new FirstBindingAtom(nonGroundRule, atomPos, bodyAtom));
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
			Predicate predicate = facts.getKey();
			knownPredicates.add(predicate);
			predicateInstances.putIfAbsent(predicate, new TreeSet<>());
			for (Instance instance : facts.getValue()) {
				predicateInstances.get(predicate).add(new BasicAtom(predicate, instance.terms));
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
			// Instead of generating NoGoods, add instance to working memories directly.
			workingMemory.addInstances(predicate, true, factsFromProgram.get(predicate));
		}

		for (NonGroundRule nonGroundRule : rulesFromProgram) {
			// Generate nogoods for all rules that are ground already.
			if (!nonGroundRule.isOriginallyGround()) {
				continue;
			}

			// Check if rule contains intervals (but is otherwise ground).
			if (nonGroundRule.containsIntervals()) {
				// Then generate all substitutions of the intervals and generate the resulting nogoods.
				List<Substitution> substitutions = bindNextAtomInRule(nonGroundRule, 0, -1, new Substitution(), null);
				for (Substitution substitution : substitutions) {
					registry.register(generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution), noGoodsFromFacts);
				}
			} else {
				// Generate nogoods of ground rules (where no intervals occur).
				registry.register(generateNoGoodsFromGroundSubstitution(nonGroundRule, new Substitution()), noGoodsFromFacts);
			}
		}

		return noGoodsFromFacts;
	}

	private NoGood supportednessNoGoodUniqueRule(int headAtom, int ruleBodyAtom) {
		return new NoGood(headAtom, -ruleBodyAtom);
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
		for (IndexedInstanceStorage modifiedWorkingMemory : workingMemory.modified()) {

			// Iterate over all rules whose body contains the interpretation corresponding to the current workingMemory.
			ArrayList<FirstBindingAtom> firstBindingAtoms = rulesUsingPredicateWorkingMemory.get(modifiedWorkingMemory);
			// Skip working memories that are not used by any rule.
			if (firstBindingAtoms == null) {
				continue;
			}

			for (FirstBindingAtom firstBindingAtom : firstBindingAtoms) {
				// Use the recently added instances from the modified working memory to construct an initial substitution
				NonGroundRule nonGroundRule = firstBindingAtom.rule;
				List<Substitution> substitutions = new ArrayList<>();

				// Generate substitutions from each recent instance.
				for (Instance instance : modifiedWorkingMemory.getRecentlyAddedInstances()) {
					// Check instance if it matches with the atom.

					Substitution unified = unify(firstBindingAtom.firstBindingAtom, instance, new Substitution());
					if (unified != null) {
						substitutions.addAll(bindNextAtomInRule(nonGroundRule, 0, firstBindingAtom.firstBindingAtomPos, unified, currentAssignment));
					}
				}

				for (Substitution substitution : substitutions) {
					registry.register(generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution), newNoGoods);
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
				LOGGER.debug("{} == {}", noGoodEntry.getValue(), noGoodToString(noGoodEntry.getValue()));
			}
			LOGGER.debug("Choice information is:");
			for (Map.Entry<Integer, Integer> enablers : newChoiceAtoms.getLeft().entrySet()) {
				LOGGER.debug("{} enabled by {}.", enablers.getKey(), enablers.getValue());
			}
			for (Map.Entry<Integer, Integer> disablers : newChoiceAtoms.getRight().entrySet()) {
				LOGGER.debug("{} disabled by {}.", disablers.getKey(), disablers.getValue());
			}
		}
		return newNoGoods;
	}

	boolean disableInstanceRemoval;

	@Override
	public int register(NoGood noGood) {
		return registry.register(noGood);
	}

	/**
	 * Generates all NoGoods resulting from a non-ground rule and a variable substitution.
	 * @param nonGroundRule
	 * @param substitution
	 * @return
	 */
	private List<NoGood> generateNoGoodsFromGroundSubstitution(NonGroundRule nonGroundRule, Substitution substitution) {
		if (LOGGER.isDebugEnabled()) {
			// Debugging helper: record known grounding substitutions.
			knownGroundingSubstitutions.putIfAbsent(nonGroundRule, new LinkedHashSet<>());
			knownGroundingSubstitutions.get(nonGroundRule).add(substitution);
		}

		final List<Integer> pos = collectPos(nonGroundRule, substitution);
		final List<Integer> neg = collectNeg(nonGroundRule, substitution);

		if (pos == null || neg == null) {
			return emptyList();
		}

		// A constraint is represented by exactly one NoGood.
		if (nonGroundRule.isConstraint()) {
			return translateConstraint(pos, neg);
		}

		// Prepare atom representing the rule body
		final Atom ruleBodyAtom = new RuleAtom(nonGroundRule, substitution);

		// Check uniqueness of ground rule by testing whether the body representing atom already has an id
		if (atomStore.contains(ruleBodyAtom)) {
			// The current ground instance already exists, therefore all NoGoods have already been created.
			return emptyList();
		}

		final int bodyAtom = atomStore.add(ruleBodyAtom);
		final int headAtom = atomStore.add(nonGroundRule.getHeadAtom().substitute(substitution));

		final List<NoGood> generatedNoGoods = new ArrayList<>();

		// Create NoGood for body.
		final NoGood ruleBody = translateBody(pos, neg, bodyAtom);
		generatedNoGoods.add(ruleBody);

		// Generate NoGoods such that the atom representing the body is true iff the body is true.
		for (int j = 1; j < ruleBody.size(); j++) {
			generatedNoGoods.add(new NoGood(bodyAtom, -ruleBody.getLiteral(j)));
		}
		generatedNoGoods.add(NoGood.headFirst(-headAtom, bodyAtom));

		// Check if the rule head is unique, add support then:
		if (uniqueGroundRulePerGroundHead.contains(nonGroundRule)) {
			generatedNoGoods.add(supportednessNoGoodUniqueRule(headAtom, bodyAtom));
		}

		// Check if the body of the rule contains negation, add choices then
		if (neg.size() != 0) {
			generatedNoGoods.addAll(generateAndAddChoices(pos, neg, bodyAtom));
		}

		return generatedNoGoods;
	}

	private List<Integer> collectNeg(NonGroundRule nonGroundRule, Substitution substitution) {
		final List<Integer> neg = new ArrayList<>(nonGroundRule.getBodyAtomsNegative().size());
		for (Atom atom : nonGroundRule.getBodyAtomsNegative()) {
			final Atom groundAtom = atom.substitute(substitution);

			HashSet<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Negative atom that is always true encountered, skip whole rule as it will never fire.
				return null;
			}

			HashSet<NonGroundRule> definingRules = ruleHeadsToDefiningRules.get(groundAtom.getPredicate());
			if (definingRules == null || definingRules.isEmpty()) {
				// Negative atom is no fact and no rule defines it, it is always false, skip it.
				continue;
			}

			neg.add(atomStore.add(groundAtom));
		}
		return neg;
	}

	private List<Integer> collectPos(NonGroundRule nonGroundRule, Substitution substitution) {
		final List<Integer> pos = new ArrayList<>(nonGroundRule.getBodyAtomsNegative().size());

		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom instanceof ExternalAtom) {
				ExternalAtom external = (ExternalAtom) atom;

				if (external.hasOutput()) {
					continue;
				}

				// Truth of builtin atoms does not depend on any assignment
				// hence, they need not be represented as long as they evaluate to true
				List<Substitution> substitutions = external.getSubstitutions(substitution);

				if (substitutions.isEmpty()) {
					return null;
				}
				continue;
			}

			if (atom instanceof IntervalAtom) {
				// IntervalAtoms are needed for deriving all substitutions of intervals but otherwise can be ignored.
				continue;
			}

			Atom groundAtom = atom.substitute(substitution);
			// Consider facts to eliminate ground atoms from the generated nogoods that are always true
			// and eliminate nogoods that are always satisfied due to facts.
			HashSet<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Skip positive atoms that are always true.
				continue;
			}

			HashSet<NonGroundRule> definingRules = ruleHeadsToDefiningRules.get(groundAtom.getPredicate());
			if (definingRules == null || definingRules.isEmpty()) {
				// Atom is no fact and no rule defines it, it cannot be derived (i.e., is always false), skip whole rule as it will never fire.
				return null;
			}
			pos.add(atomStore.add(groundAtom));
		}
		return pos;
	}

	private NoGood translateBody(List<Integer> pos, List<Integer> neg, int bodyAtom) {
		int[] bodyLiterals = new int[pos.size() + neg.size() + 1];
		bodyLiterals[0] = -bodyAtom;

		int i = 1;
		for (Integer atomId : pos) {
			bodyLiterals[i++] = atomId;
		}
		for (Integer atomId : neg) {
			bodyLiterals[i++] = -atomId;
		}

		return NoGood.headFirst(bodyLiterals);
	}

	private List<NoGood> generateAndAddChoices(List<Integer> pos, List<Integer> neg, int bodyAtom) {
		List<NoGood> noGoods = new ArrayList<>(neg.size() + 1);
		Map<Integer, Integer> newChoiceOn = newChoiceAtoms.getLeft();
		Map<Integer, Integer> newChoiceOff = newChoiceAtoms.getRight();
		// Choice is on the body representing atom

		// ChoiceOn if all positive body atoms are satisfied
		int[] choiceOnLiterals = new int[pos.size() + 1];
		int i = 1;
		for (Integer atomId : pos) {
			choiceOnLiterals[i++] = atomId;
		}

		int choiceId = choiceAtomsGenerator.getNextId();
		Atom choiceOnAtom = ChoiceAtom.on(choiceId);

		int choiceOnAtomIdInt = atomStore.add(choiceOnAtom);
		choiceOnLiterals[0] = -choiceOnAtomIdInt;
		// Add corresponding NoGood and ChoiceOn
		// ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.
		noGoods.add(NoGood.headFirst(choiceOnLiterals));
		newChoiceOn.put(bodyAtom, choiceOnAtomIdInt);

		// ChoiceOff if some negative body atom is contradicted
		Atom choiceOffAtom = ChoiceAtom.off(choiceId);
		int choiceOffAtomIdInt = atomStore.add(choiceOffAtom);
		for (Integer negAtomId : neg) {
			// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
			noGoods.add(NoGood.headFirst(-choiceOffAtomIdInt, negAtomId));
		}
		newChoiceOff.put(bodyAtom, choiceOffAtomIdInt);

		return noGoods;
	}

	private List<NoGood> translateConstraint(List<Integer> positive, List<Integer> negative) {
		int[] constraintLiterals = new int[positive.size() + negative.size()];
		int i = 0;
		for (Integer atomId : positive) {
			constraintLiterals[i++] = +atomId;
		}
		for (Integer atomId : negative) {
			constraintLiterals[i++] = -atomId;
		}
		return singletonList(new NoGood(constraintLiterals));
	}

	private LinkedHashSet<Atom> removeAfterObtainingNewNoGoods = new LinkedHashSet<>();
	private int maxAtomIdBeforeGroundingNewNoGoods = -1;

	private List<Substitution> bindNextAtomInRule(NonGroundRule rule, int atomPos, int firstBindingPos, Substitution partialSubstitution, Assignment currentAssignment) {
		if (atomPos == rule.getNumBodyAtoms()) {
			return Collections.singletonList(partialSubstitution);
		}

		if (atomPos == firstBindingPos) {
			// Binding for this position was already computed, skip it.
			return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution, currentAssignment);
		}

		Literal currentAtom = rule.getBodyAtom(atomPos);
		if (currentAtom instanceof ExternalAtom) {
			// Assumption: all variables occurring in the builtin atom are already bound
			// (as ensured by the body atom sorting)

			// Generate all substitutions for the interval representing variable.
			List<Substitution> substitutions = ((ExternalAtom)currentAtom).getSubstitutions(partialSubstitution);

			if (substitutions.isEmpty()) {
				return emptyList();
			}

			ArrayList<Substitution> generatedSubstitutions = new ArrayList<>();
			for (Substitution intervalSubstitution : substitutions) {
				// Continue grounding with each of the generated values.
				generatedSubstitutions.addAll(bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, intervalSubstitution, currentAssignment));
			}
			return generatedSubstitutions;
		}
		if (currentAtom instanceof IntervalAtom) {
			// Assumption: IntervalAtoms occur before all BuiltinAtoms and after all positive ordinary atoms in the body, to have their values bound at evaluation.
			// Generate the set of substitutions stemming from this interval specification.
			IntervalAtom groundInterval = (IntervalAtom) currentAtom.substitute(partialSubstitution);	// Substitute variables occurring in the interval itself.

			// Generate all substitutions for the interval representing variable.
			List<Substitution> intervalSubstitutions = groundInterval.getIntervalSubstitutions(partialSubstitution);
			ArrayList<Substitution> generatedSubstitutions = new ArrayList<>();
			for (Substitution intervalSubstitution : intervalSubstitutions) {
				// Continue grounding with each of the generated values.
				generatedSubstitutions.addAll(bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, intervalSubstitution, currentAssignment));
			}
			return generatedSubstitutions;
		}

		// check if partialVariableSubstitution already yields a ground atom
		final Atom substitute = currentAtom.substitute(partialSubstitution);

		if (substitute.isGround()) {
			// Substituted atom is ground, in case it is positive, only ground if it also holds true
			if (!rule.isBodyAtomPositive(atomPos)) {
				// Atom occurs negated in the rule, continue grounding
				return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution, currentAssignment);
			}

			IndexedInstanceStorage wm = workingMemory.get(currentAtom, true);
			if (!wm.containsInstance(new Instance(substitute.getTerms()))) {
				// Generate no variable substitution.
				return emptyList();
			}

			// Check if atom is also assigned true.
			if (!(factsFromProgram.get(substitute.getPredicate()) == null || !factsFromProgram.get(substitute.getPredicate()).contains(new Instance(substitute.getTerms())))) {
				// Ground literal holds, continue finding a variable substitution.
				return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution, currentAssignment);
			}

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

		// substituted atom contains variables
		IndexedInstanceStorage storage = workingMemory.get(currentAtom);
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
			generatedSubstitutions.addAll(bindInstance(substitute, instance, rule, atomPos, firstBindingPos, partialSubstitution, currentAssignment));
		}

		return generatedSubstitutions;
	}

	private List<Substitution> bindInstance(Atom substitute, Instance instance, NonGroundRule rule, int atomPos, int firstBindingPos, Substitution partialSubstitution, Assignment currentAssignment) {
		// Check each instance if it matches with the atom.
		Substitution unified = unify(substitute, instance, new Substitution(partialSubstitution));
		if (unified == null) {
			return emptyList();
		}

		// Check if atom is also assigned true.
		BasicAtom substituteClone = new BasicAtom((BasicAtom) substitute);
		Atom substitutedAtom = substituteClone.substitute(unified);
		if (!substitutedAtom.isGround()) {
			throw new RuntimeException("Grounded atom should be ground but is not. Should not happen.");
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
						return emptyList();
					}
				}
			}
		}

		return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, unified, currentAssignment);
	}

	/**
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param substitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	protected Substitution unify(Atom atom, Instance instance, Substitution substitution) {
		for (int i = 0; i < instance.terms.size(); i++) {
			if (instance.terms.get(i) == atom.getTerms().get(i) ||
				substitution.unifyTerms(atom.getTerms().get(i), instance.terms.get(i))) {
				continue;
			}
			return null;
		}
		return substitution;
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> currentChoiceAtoms = newChoiceAtoms;
		newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
		return currentChoiceAtoms;
	}

	@Override
	public void updateAssignment(Iterator<Assignment.Entry> it) {
		while (it.hasNext()) {
			Assignment.Entry assignment = it.next();
			workingMemory.addInstance(atomStore.get(assignment.getAtom()), assignment.getTruth().toBoolean());
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
		public NonGroundRule rule;
		public int firstBindingAtomPos;
		public Atom firstBindingAtom;

		public FirstBindingAtom(NonGroundRule rule, int firstBindingAtomPos, Atom firstBindingAtom) {
			this.rule = rule;
			this.firstBindingAtomPos = firstBindingAtomPos;
			this.firstBindingAtom = firstBindingAtom;
		}
	}
}
