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
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstraint;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFact;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedRule;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.Choices;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounder extends BridgedGrounder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveGrounder.class);

	/**
	 * Atoms corresponding to rule bodies use this predicate, first term is rule number,
	 * second is a term containing variable substitutions.
	 */
	public static final BasicPredicate RULE_BODIES_PREDICATE = new BasicPredicate("_R_", 2);

	public static final BasicPredicate CHOICE_ON_PREDICATE = new BasicPredicate("ChoiceOn", 1);
	public static final BasicPredicate CHOICE_OFF_PREDICATE = new BasicPredicate("ChoiceOff", 1);

	private final IntIdGenerator intIdGenerator = new IntIdGenerator();
	private final IntIdGenerator nogoodIdGenerator = new IntIdGenerator();
	private final IntIdGenerator choiceAtomsGenerator = new IntIdGenerator();

	private final HashMap<Predicate, ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage>> workingMemory = new HashMap<>();
	private final Map<NoGood, Integer> nogoodIdentifiers = new HashMap<>();
	private final AtomStore atomStore = new AtomStore();

	private final HashMap<Predicate, ArrayList<Instance>> factsFromProgram = new HashMap<>();
	private final ArrayList<NonGroundRule> rulesFromProgram = new ArrayList<>();
	private final HashMap<IndexedInstanceStorage, ArrayList<FirstBindingAtom>> rulesUsingPredicateWorkingMemory = new HashMap<>();
	private final HashSet<Predicate> knownPredicates = new HashSet<>();
	private final HashMap<NonGroundRule, HashSet<VariableSubstitution>> knownGroundingSubstitutions = new HashMap<>();

	private boolean outputFactNogoods = true;

	private HashSet<IndexedInstanceStorage> modifiedWorkingMemories = new HashSet<>();
	private Choices choices = new Choices();

	public NaiveGrounder(ParsedProgram program, Bridge... bridges) {
		this(program, p -> true, bridges);
	}

	public NaiveGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		super(program, filter, bridges);

		// initialize all facts
		for (ParsedFact fact : this.program.facts) {
			String predicateName = fact.getFact().getPredicate();
			int predicateArity = fact.getFact().getArity();

			BasicPredicate predicate = new BasicPredicate(predicateName, predicateArity);
			// Record predicate
			adaptWorkingMemoryForPredicate(predicate);

			// Construct instance from the fact.
			ArrayList<Term> termList = new ArrayList<>();
			for (int i = 0; i < predicateArity; i++) {
				termList.add(fact.getFact().getTerms().get(i).toTerm());
			}
			Instance instance = new Instance(termList.toArray(new Term[0]));
			// Add instance to corresponding list of facts
			factsFromProgram.putIfAbsent(predicate, new ArrayList<>());
			ArrayList<Instance> internalPredicateInstances = factsFromProgram.get(predicate);
			internalPredicateInstances.add(instance);
		}
		// initialize rules
		adaptWorkingMemoryForPredicate(RULE_BODIES_PREDICATE);
		adaptWorkingMemoryForPredicate(CHOICE_ON_PREDICATE);
		adaptWorkingMemoryForPredicate(CHOICE_OFF_PREDICATE);
		for (ParsedRule rule : program.rules) {
			registerRuleOrConstraint(rule);
		}
		// initialize constraints
		for (ParsedConstraint constraint : program.constraints) {
			registerRuleOrConstraint(new ParsedRule(constraint.body));
		}
		// Hint: Could clear this.program to free memory.
		this.program = null;
	}

	private void adaptWorkingMemoryForPredicate(Predicate predicate) {
		// Create working memory for predicate if it does not exist
		if (!workingMemory.containsKey(predicate)) {
			IndexedInstanceStorage instanceStoragePlus = new IndexedInstanceStorage(predicate.getPredicateName() + "+", predicate.getArity());
			IndexedInstanceStorage instanceStorageMinus = new IndexedInstanceStorage(predicate.getPredicateName() + "-", predicate.getArity());
			// Index all positions of the storage (may impair efficiency)
			for (int i = 0; i < predicate.getArity(); i++) {
				instanceStoragePlus.addIndexPosition(i);
				instanceStorageMinus.addIndexPosition(i);
			}
			workingMemory.put(predicate, new ImmutablePair<>(instanceStoragePlus, instanceStorageMinus));
		}
		knownPredicates.add(predicate);
	}

	private void registerRuleOrConstraint(ParsedRule rule) {
		// Record the rule for later use
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(intIdGenerator, rule);
		// Create working memories for all predicates occurring in the rule
		for (Predicate predicate : nonGroundRule.getOccurringPredicates()) {
			adaptWorkingMemoryForPredicate(predicate);
		}
		rulesFromProgram.add(nonGroundRule);

		// Register the rule at the working memory corresponding to its first predicate.
		Predicate firstBodyPredicate = nonGroundRule.usedFirstBodyPredicate();
		if (firstBodyPredicate == null) {
			// No ordinary first body predicate, hence it only contains ground builtin predicates.
			return;
		}
		// Register each atom occurring in the body of the rule at its corresponding working memory.
		HashSet<BasicAtom> registeredPositiveAtoms = new HashSet<>();
		for (int i = 0; i < nonGroundRule.getBodyAtomsPositive().size(); i++) {
			registerAtomAtWorkingMemory(true, nonGroundRule, registeredPositiveAtoms, i);
		}
		// Register negative literals only if the rule contains no positive literals (necessary grounding is ensured by safety of rules).
		if (nonGroundRule.getBodyAtomsPositive().size() == 0) {
			HashSet<BasicAtom> registeredNegativeAtoms = new HashSet<>();
			for (int i = 0; i < nonGroundRule.getBodyAtomsNegative().size(); i++) {
				registerAtomAtWorkingMemory(false, nonGroundRule, registeredNegativeAtoms, i);
			}
		}
	}

	/**
	 * Registers an atom occurring in a rule at its corresponding working memory if it has not already been treated this way.
	 * @param isPositive indicates whether the atom occurs positively or negatively in the rule.
	 * @param nonGroundRule the rule into which the atom occurs.
	 * @param registeredAtoms a set of already registered atoms (will skip if the predicate of the current atom occurs in this set). This set will be extended by the current atom.
	 * @param atomPos the position in the rule of the atom.
	 */
	private void registerAtomAtWorkingMemory(boolean isPositive, NonGroundRule nonGroundRule, HashSet<BasicAtom> registeredAtoms, int atomPos) {
		Atom bodyAtom = isPositive ? nonGroundRule.getBodyAtomsPositive().get(atomPos) : nonGroundRule.getBodyAtomsNegative().get(atomPos);
		if ((bodyAtom instanceof BasicAtom) && !registeredAtoms.contains(bodyAtom)) {
			Predicate predicate = ((BasicAtom) bodyAtom).predicate;
			registeredAtoms.add((BasicAtom) bodyAtom);
			IndexedInstanceStorage workingMemory = isPositive ? this.workingMemory.get(predicate).getLeft() : this.workingMemory.get(predicate).getRight();
			rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemory, new ArrayList<>());
			rulesUsingPredicateWorkingMemory.get(workingMemory).add(
				new FirstBindingAtom(nonGroundRule, atomPos, (BasicAtom) bodyAtom));
		}
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		Map<Predicate, SortedSet<Atom>> predicateInstances = new HashMap<>();
		SortedSet<Predicate> knownPredicates = new TreeSet<>();

		if (!trueAtoms.iterator().hasNext()) {
			return BasicAnswerSet.EMPTY;
		}

		// Iterate over all true atomIds, computeNextAnswerSet instances from atomStore and add them if not filtered.
		for (int trueAtom : trueAtoms) {
			Atom basicAtom = atomStore.get(trueAtom);
			Predicate predicate = basicAtom.getPredicate();

			// Skip internal atoms and filtered predicates.
			if (basicAtom.isInternal() || !filter.test(predicate)) {
				continue;
			}

			knownPredicates.add(basicAtom.getPredicate());
			predicateInstances.putIfAbsent(basicAtom.getPredicate(), new TreeSet<>());
			SortedSet<Atom> instances = predicateInstances.get(basicAtom.getPredicate());
			instances.add(basicAtom);
		}

		if (knownPredicates.isEmpty()) {
			return BasicAnswerSet.EMPTY;
		}

		return new BasicAnswerSet(knownPredicates, predicateInstances);
	}

	/**
	 * Derives all NoGoods representing facts of the input program. May only be called once.
	 * @return
	 */
	private Map<Integer, NoGood> noGoodsFromFacts() {
		HashMap<Integer, NoGood> noGoodsFromFacts = new HashMap<>();
		for (Predicate predicate : factsFromProgram.keySet()) {
			for (Instance instance : factsFromProgram.get(predicate)) {
				int atomIdFactAtom = atomStore.add(new BasicAtom(predicate, false, instance.terms));
				NoGood noGood = NoGood.headFirst(-atomIdFactAtom);
				// The noGood is assumed to be new.
				int noGoodId = nogoodIdGenerator.getNextId();
				nogoodIdentifiers.put(noGood, noGoodId);
				noGoodsFromFacts.put(noGoodId, noGood);
			}
		}
		for (NonGroundRule nonGroundRule : rulesFromProgram) {
			if (!nonGroundRule.isGround()) {
				continue;
			}
			List<NoGood> noGoods = generateNoGoodsFromGroundSubstitution(nonGroundRule, new VariableSubstitution());
			for (NoGood noGood : noGoods) {
				// Check if noGood was already derived earlier and add it is new
				if (!nogoodIdentifiers.containsKey(noGood)) {
					int noGoodId = nogoodIdGenerator.getNextId();
					nogoodIdentifiers.put(noGood, noGoodId);
					noGoodsFromFacts.put(noGoodId, noGood);
				}
				registerIfNeeded(noGood, noGoodsFromFacts);
			}
		}
		return noGoodsFromFacts;
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(ReadableAssignment assignment) {
		// First call, output all NoGoods from facts.
		if (outputFactNogoods) {
			outputFactNogoods = false;
			return noGoodsFromFacts();
		}

		// Compute new ground rule (evaluate joins with newly changed atoms)
		HashMap<Integer, NoGood> newNoGoods = new HashMap<>();
		for (IndexedInstanceStorage modifiedWorkingMemory : modifiedWorkingMemories) {

			// Iterate over all rules whose body contains the predicate corresponding to the current workingMemory.
			ArrayList<FirstBindingAtom> firstBindingAtoms = rulesUsingPredicateWorkingMemory.get(modifiedWorkingMemory);
			// Skip working memories that are not used by any rule.
			if (firstBindingAtoms == null) {
				continue;
			}
			for (FirstBindingAtom firstBindingAtom : firstBindingAtoms) {
				// Use the recently added instances from the modified working memory to construct an initial variableSubstitution
				NonGroundRule nonGroundRule = firstBindingAtom.rule;
				List<VariableSubstitution> variableSubstitutions = new ArrayList<>();
				// Generate variableSubstitutions from each recent instance.
				for (Instance instance : modifiedWorkingMemory.getRecentlyAddedInstances()) {
					// Check instance if it matches with the atom.
					VariableSubstitution partialVariableSubstitution = new VariableSubstitution();
					if (unify(firstBindingAtom.firstBindingAtom, instance, partialVariableSubstitution)) {
						variableSubstitutions.addAll(bindNextAtomInRule(nonGroundRule, 0, firstBindingAtom.firstBindingAtomPos, partialVariableSubstitution));
					}
				}
				for (VariableSubstitution variableSubstitution : variableSubstitutions) {
					List<NoGood> noGoods = generateNoGoodsFromGroundSubstitution(nonGroundRule, variableSubstitution);
					for (NoGood noGood : noGoods) {
						// Check if noGood was already derived earlier, add if it is new
						registerIfNeeded(noGood, newNoGoods);
					}
				}
			}

			// Mark instances added by updateAssignment as done
			modifiedWorkingMemory.markRecentlyAddedInstancesDone();
		}

		// Import additional noGoods from external sources
		for (NoGood noGood : collectExternalNogoods(assignment, atomStore, choices, choiceAtomsGenerator)) {
			registerIfNeeded(noGood, newNoGoods);
		}

		modifiedWorkingMemories = new HashSet<>();
		return newNoGoods;
	}

	/**
	 * Generates all NoGoods resulting from a non-ground rule and a variable substitution.
	 * @param nonGroundRule
	 * @param variableSubstitution
	 * @return
	 */
	private List<NoGood> generateNoGoodsFromGroundSubstitution(NonGroundRule nonGroundRule, VariableSubstitution variableSubstitution) {
		if (LOGGER.isDebugEnabled()) {
			// Debugging helper: record known grounding substitutions.
			knownGroundingSubstitutions.putIfAbsent(nonGroundRule, new HashSet<>());
			knownGroundingSubstitutions.get(nonGroundRule).add(variableSubstitution);
		}

		// Collect ground atoms in the body
		ArrayList<Integer> bodyAtomsPositive = new ArrayList<>();
		ArrayList<Integer> bodyAtomsNegative = new ArrayList<>();

		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom instanceof BuiltinAtom) {
				// Truth of builtin atoms does not depend on any assignment
				// hence, they need not be represented as long as they evaluate to true
				if (((BuiltinAtom) atom).evaluate(variableSubstitution)) {
					continue;
				} else {
					// Rule body is always false, skip the whole rule.
					return Collections.emptyList();
				}
			}
			int groundAtomPositive = SubstitutionUtil.groundingSubstitute(atomStore, atom, variableSubstitution);
			bodyAtomsPositive.add(groundAtomPositive);
		}

		for (Atom basicAtom : nonGroundRule.getBodyAtomsNegative()) {
			int groundAtomNegative = SubstitutionUtil.groundingSubstitute(atomStore, basicAtom, variableSubstitution);
			bodyAtomsNegative.add(-groundAtomNegative);
		}

		final int bodySize = bodyAtomsPositive.size() + bodyAtomsNegative.size();

		if (nonGroundRule.isConstraint()) {
			// A constraint is represented by one NoGood.
			int[] constraintLiterals = new int[bodySize];
			int i = 0;
			for (Integer atomId : Iterables.concat(bodyAtomsPositive, bodyAtomsNegative)) {
				constraintLiterals[i++] = atomId;
			}

			return Collections.singletonList(new NoGood(constraintLiterals));
		}

		// Prepare atom representing the rule body
		BasicAtom ruleBodyRepresentingPredicate = new BasicAtom(RULE_BODIES_PREDICATE,
			true, ConstantTerm.getInstance(Integer.toString(nonGroundRule.getRuleId())),
			ConstantTerm.getInstance(variableSubstitution.toUniformString()));

		// Check uniqueness of ground rule by testing whether the body representing atom already has an id
		if (atomStore.contains(ruleBodyRepresentingPredicate)) {
			// The current ground instance already exists, therefore all NoGoods have already been created.
			return Collections.emptyList();
		}

		int bodyRepresentingAtomId = atomStore.add(ruleBodyRepresentingPredicate);

		// Prepare head atom
		int headAtomId = SubstitutionUtil.groundingSubstitute(atomStore, nonGroundRule.getHeadAtom(), variableSubstitution);

		// Create NoGood for body.
		int[] bodyLiterals = new int[bodySize + 1];
		bodyLiterals[0] = -bodyRepresentingAtomId;
		int i = 1;
		for (Integer atomId : Iterables.concat(bodyAtomsPositive, bodyAtomsNegative)) {
			bodyLiterals[i++] = atomId;
		}

		final int capacity = 2 + bodyLiterals.length - 1 + (bodyAtomsNegative.isEmpty() ? 0 : bodyAtomsNegative.size() + 1);

		List<NoGood> generatedNoGoods = new ArrayList<>(capacity);

		generatedNoGoods.add(NoGood.headFirst(bodyLiterals));
		generatedNoGoods.add(NoGood.headFirst(-headAtomId, bodyRepresentingAtomId));

		// Generate NoGoods such that the atom representing the body is true iff the body is true.
		for (int j = 1; j < bodyLiterals.length; j++) {
			generatedNoGoods.add(new NoGood(bodyRepresentingAtomId, -bodyLiterals[j]));
		}

		// Check if the body of the rule contains negation, add choices then
		if (!bodyAtomsNegative.isEmpty()) {
			// Choice is on the body representing atom
			int choiceId = choiceAtomsGenerator.getNextId();

			// ChoiceOn if all positive body atoms are satisfied
			int choiceOnAtomIdInt = atomStore.add(ChoiceAtom.on(choiceId));
			int[] choiceOnLiterals = new int[bodyAtomsPositive.size() + 1];
			choiceOnLiterals[0] = -choiceOnAtomIdInt;
			i = 1;
			for (Integer atomId : bodyAtomsPositive) {
				choiceOnLiterals[i++] = atomId;
			}
			// Add corresponding NoGood and ChoiceOn
			generatedNoGoods.add(NoGood.headFirst(choiceOnLiterals));	// ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.

			// ChoiceOff if some negative body atom is contradicted
			int choiceOffAtomIdInt = atomStore.add(ChoiceAtom.off(choiceId));
			for (Integer negAtomId : bodyAtomsNegative) {
				// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
				generatedNoGoods.add(NoGood.headFirst(-choiceOffAtomIdInt, -negAtomId));
			}
			choices.put(bodyRepresentingAtomId, choiceOnAtomIdInt, choiceOffAtomIdInt);
		}
		return generatedNoGoods;
	}

	private List<VariableSubstitution> bindNextAtomInRule(NonGroundRule rule, int atomPos, int firstBindingPos, VariableSubstitution partialVariableSubstitution) {
		if (atomPos == rule.getNumBodyAtoms()) {
			return Collections.singletonList(partialVariableSubstitution);
		}

		if (atomPos == firstBindingPos) {
			// Binding for this position was already computed, skip it.
			return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialVariableSubstitution);
		}

		Atom currentAtom = rule.getBodyAtom(atomPos);
		if (currentAtom instanceof BuiltinAtom) {
			// Assumption: all variables occurring in the builtin atom are already bound
			// (as ensured by the body atom sorting)
			if (((BuiltinAtom)currentAtom).evaluate(partialVariableSubstitution)) {
				// Builtin is true, continue with next atom in rule body.
				return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialVariableSubstitution);
			}

			// Builtin is false, return no bindings.
			return Collections.emptyList();
		}

		// check if partialVariableSubstitution already yields a ground atom
		BasicAtom currentBasicAtom = (BasicAtom)currentAtom;
		Pair<Boolean, BasicAtom> substitute = SubstitutionUtil.substitute(currentBasicAtom, partialVariableSubstitution);
		if (substitute.getLeft()) {
			// Substituted atom is ground, in case it is positive, only ground if it also holds true
			if (rule.isBodyAtomPositive(atomPos)) {
				IndexedInstanceStorage wm = workingMemory.get(currentBasicAtom.predicate).getLeft();
				if (wm.containsInstance(new Instance(substitute.getRight().termList))) {
					// Ground literal holds, continue finding a variable substitution.
					return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialVariableSubstitution);
				}

				// Generate no variable substitution.
				return Collections.emptyList();
			}

			// Atom occurs negated in the rule, continue grounding
			return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialVariableSubstitution);
		}

		// substituted atom contains variables
		ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> wms = workingMemory.get(currentBasicAtom.predicate);
		IndexedInstanceStorage storage = rule.isBodyAtomPositive(atomPos) ? wms.getLeft() : wms.getRight();
		Collection<Instance> instances;
		if (partialVariableSubstitution.substitution.isEmpty()) {
			// No variables are bound, but first atom in the body became recently true, consider all instances now.
			instances = storage.getAllInstances();
		} else {
			// For selection of the instances, find ground term on which to select
			int firstGroundTermPos = 0;
			Term firstGroundTerm = null;
			for (int i = 0; i < substitute.getRight().termList.length; i++) {
				Term testTerm = substitute.getRight().termList[i];
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

		ArrayList<VariableSubstitution> generatedSubstitutions = new ArrayList<>();
		for (Instance instance : instances) {
			// Check each instance if it matches with the atom.
			VariableSubstitution variableSubstitutionClone = new VariableSubstitution(partialVariableSubstitution);
			if (unify(substitute.getRight(), instance, variableSubstitutionClone)) {
				generatedSubstitutions.addAll(bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, variableSubstitutionClone));
			}
		}

		return generatedSubstitutions;
	}

	/**
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param variableSubstitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	protected boolean unify(BasicAtom atom, Instance instance, VariableSubstitution variableSubstitution) {
		VariableSubstitution tempVariableSubstitution = new VariableSubstitution(variableSubstitution);
		for (int i = 0; i < instance.terms.length; i++) {
			if (instance.terms[i] == atom.termList[i] ||
				unifyTerms(atom.termList[i], instance.terms[i], tempVariableSubstitution)) {
				continue;
			}
			return false;
		}
		variableSubstitution.replaceSubstitution(tempVariableSubstitution);
		return true;
	}

	/**
	 * Checks if the left possible non-ground term unifies with the ground term.
	 * @param termNonGround
	 * @param termGround
	 * @param variableSubstitution
	 * @return
	 */
	boolean unifyTerms(Term termNonGround, Term termGround, VariableSubstitution variableSubstitution) {
		if (termNonGround == termGround) {
			// Both terms are either the same constant or the same variable term
			return true;
		} else if (termNonGround instanceof ConstantTerm) {
			// Since right term is ground, both terms differ
			return false;
		} else if (termNonGround instanceof VariableTerm) {
			// Left term is variable, bind it to the right term.
			if (variableSubstitution.substitution.get(termNonGround) != null) {
				// Variable is already bound, return true if binding is the same as the current ground term.
				return termNonGround == variableSubstitution.substitution.get(termNonGround);
			} else {
				variableSubstitution.substitution.put((VariableTerm) termNonGround, termGround);
				return true;
			}
		} else if (termNonGround instanceof FunctionTerm && termGround instanceof FunctionTerm) {
			// Both terms are function terms
			FunctionTerm ftNonGround = (FunctionTerm) termNonGround;
			FunctionTerm ftGround = (FunctionTerm) termGround;

			if (ftNonGround.functionSymbol != ftGround.functionSymbol || ftNonGround.termList.size() != ftGround.termList.size()) {
				return false;
			}

			// Iterate over all subterms of both function terms
			for (int i = 0; i < ftNonGround.termList.size(); i++) {
				if (!unifyTerms(ftNonGround.termList.get(i), ftGround.termList.get(i), variableSubstitution)) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	@Override
	public Choices getChoices() {
		Choices currentChoices = choices;
		choices = new Choices();
		return currentChoices;
	}

	@Override
	public void updateAssignment(Iterator<? extends ReadableAssignment.Entry> it) {
		while (it.hasNext()) {
			ReadableAssignment.Entry assignment = it.next();
			Truth truthValue = assignment.getTruth();
			int atomId = assignment.getAtom();
			Atom atom = atomStore.get(atomId);
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
	public String atomToString(int atomId) {
		return atomStore.get(atomId).toString();
	}

	@Override
	public List<Integer> getUnassignedAtoms(ReadableAssignment assignment) {
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
		if (!nogoodIdentifiers.containsKey(noGood)) {
			int noGoodId = nogoodIdGenerator.getNextId();
			nogoodIdentifiers.put(noGood, noGoodId);
			return noGoodId;
		}
		return nogoodIdentifiers.get(noGood);
	}

	public void printCurrentlyKnownGroundRules() {
		System.out.println("Printing known ground rules:");
		for (Map.Entry<NonGroundRule, HashSet<VariableSubstitution>> ruleSubstitutionsEntry : knownGroundingSubstitutions.entrySet()) {
			NonGroundRule nonGroundRule = ruleSubstitutionsEntry.getKey();
			HashSet<VariableSubstitution> variableSubstitutions = ruleSubstitutionsEntry.getValue();
			for (VariableSubstitution variableSubstitution : variableSubstitutions) {
				System.out.println(SubstitutionUtil.groundAndPrintRule(nonGroundRule, variableSubstitution));
			}
		}
	}

	public void printCurrentlyKnownNoGoods() {
		System.out.println("Printing known NoGoods:");
		for (Map.Entry<NoGood, Integer> noGoodEntry : nogoodIdentifiers.entrySet()) {
			System.out.println(noGoodEntry.getKey().toStringReadable(this));
		}
	}

	private boolean registerIfNeeded(NoGood noGood, Map<Integer, NoGood> target) {
		if (nogoodIdentifiers.containsKey(noGood)) {
			return false;
		}
		int noGoodId = nogoodIdGenerator.getNextId();
		nogoodIdentifiers.put(noGood, noGoodId);
		target.put(noGoodId, noGood);
		return true;
	}

	private static class FirstBindingAtom {
		public NonGroundRule rule;

		public int firstBindingAtomPos;
		public BasicAtom firstBindingAtom;

		public FirstBindingAtom(NonGroundRule rule, int firstBindingAtomPos, BasicAtom firstBindingAtom) {
			this.rule = rule;
			this.firstBindingAtomPos = firstBindingAtomPos;
			this.firstBindingAtom = firstBindingAtom;
		}
	}

	public static class VariableSubstitution {
		HashMap<VariableTerm, Term> substitution = new HashMap<>();

		public VariableSubstitution() {
		}

		public VariableSubstitution(VariableSubstitution clone) {
			this.substitution = new HashMap<>(clone.substitution);
		}

		public void replaceSubstitution(VariableSubstitution other) {
			this.substitution = other.substitution;
		}

		/**
		 * Prints the variable substitution in a uniform way (sorted by variable names).
		 *
		 * @return
		 */
		public String toUniformString() {
			List<VariableTerm> variablesInSubstitution = new ArrayList<>(substitution.size());
			variablesInSubstitution.addAll(substitution.keySet());
			Collections.sort(variablesInSubstitution); // Hint: Maybe this is a performance issue later, better have sorted/well-defined insertion into VariableSubstitution.
			StringBuilder ret = new StringBuilder();
			for (VariableTerm variableTerm : variablesInSubstitution) {
				ret.append("_")
					.append(variableTerm)
					.append(":")
					.append(substitution.get(variableTerm));
			}
			return ret.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			VariableSubstitution that = (VariableSubstitution) o;

			return substitution != null ? substitution.equals(that.substitution) : that.substitution == null;
		}

		@Override
		public int hashCode() {
			return substitution != null ? substitution.hashCode() : 0;
		}

		public Term eval(VariableTerm variableTerm) {
			return this.substitution.get(variableTerm);
		}
	}
}
