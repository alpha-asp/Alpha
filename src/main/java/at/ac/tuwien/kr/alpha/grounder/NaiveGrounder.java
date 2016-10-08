package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstraint;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFact;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedRule;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounder extends AbstractGrounder {

	protected HashMap<Predicate, ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage>> workingMemory = new HashMap<>();
	private static final BasicPredicate RULE_BODIES_PREDICATE = new BasicPredicate("_R_", 2); // atoms corresponding
	// to rule bodies use this predicate, first term is rule number, second is a term containing variable substitutions
	private static final BasicPredicate CHOICE_ON_PREDICATE = new BasicPredicate("ChoiceOn", 1);
	private static final BasicPredicate CHOICE_OFF_PREDICATE = new BasicPredicate("ChoiceOff", 1);

	protected BidiMap<NoGood, Integer> nogoodIdentifiers = new DualHashBidiMap<>();
	private IntIdGenerator nogoodIdGenerator = new IntIdGenerator();

	private HashMap<Predicate, ArrayList<Instance>> factsFromProgram = new HashMap<>();
	private boolean outputFactNogoods = true;

	private ArrayList<NonGroundRule> rulesFromProgram = new ArrayList<>();

	private final IntIdGenerator intIdGenerator = new IntIdGenerator();

	protected AtomStore atomStore = new AtomStore();

	private HashSet<IndexedInstanceStorage> modifiedWorkingMemories = new HashSet<>();
	private HashMap<IndexedInstanceStorage, ArrayList<NonGroundRule>> rulesUsingPredicateWorkingMemory = new HashMap<>();

	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new HashMap<>(), new HashMap<>());
	private IntIdGenerator choiceAtomsGenerator = new IntIdGenerator();

	private HashSet<Predicate> knownPredicates = new HashSet<>();

	public NaiveGrounder(ParsedProgram program) {
		super(program);

		// initialize all facts
		for (ParsedFact fact : this.program.facts) {
			String predicateName = fact.fact.predicate;
			int predicateArity = fact.fact.arity;
			Predicate predicate = new BasicPredicate(predicateName, predicateArity);
			// Record predicate
			adaptWorkingMemoryForPredicate(predicate);


			IndexedInstanceStorage predicateWorkingMemoryPlus = workingMemory.get(predicate).getLeft();

			// Construct instance from the fact.
			ArrayList<Term> termList = new ArrayList<>();
			for (int i = 0; i < predicateArity; i++) {
				termList.add(AtomStore.convertFromParsedTerm(fact.fact.terms.get(i)));
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
			ParsedRule constraintWrappingRule = new ParsedRule();
			constraintWrappingRule.body = constraint.body;
			registerRuleOrConstraint(constraintWrappingRule);
		}
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
		if (nonGroundRule.isFirstBodyPredicatePositive()) {
			IndexedInstanceStorage workingMemoryPlus = workingMemory.get(firstBodyPredicate).getLeft();
			rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemoryPlus, new ArrayList<>());
			rulesUsingPredicateWorkingMemory.get(workingMemoryPlus).add(nonGroundRule);
		} else {
			IndexedInstanceStorage workingMemoryMinus = workingMemory.get(firstBodyPredicate).getRight();
			rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemoryMinus, new ArrayList<>());
			rulesUsingPredicateWorkingMemory.get(workingMemoryMinus).add(nonGroundRule);
		}
	}

	@Override
	public AnswerSet assignmentToAnswerSet(java.util.function.Predicate<Predicate> filter, int[] trueAtoms) {

		Map<Predicate, Set<PredicateInstance>> predicateInstances = new HashMap<>();
		HashSet<Predicate> knownPredicates = new HashSet<>();

		// Iterate over all true atomIds, computeNextAnswerSet instances from atomStore and add them if not filtered.
		for (int trueAtom : trueAtoms) {
			PredicateInstance predicateInstance = atomStore.getPredicateInstance(new AtomId(trueAtom));
			// Skip filtered predicates.
			if (!filter.test(predicateInstance.predicate)) {
				continue;
			}

			knownPredicates.add(predicateInstance.predicate);
			predicateInstances.putIfAbsent(predicateInstance.predicate, new HashSet<>());
			Set<PredicateInstance> instances = predicateInstances.get(predicateInstance.predicate);
			instances.add(predicateInstance);
		}
		Set<Predicate> predicateList = new HashSet<>();
		predicateList.addAll(knownPredicates);

		return new BasicAnswerSet(predicateList, predicateInstances);
	}

	/**
	 * Derives all NoGoods representing facts of the input program. May only be called once.
	 * @return
	 */
	private Map<Integer, NoGood> noGoodsFromFacts() {
		HashMap<Integer, NoGood> noGoodsFromFacts = new HashMap<>();
		for (Predicate predicate : factsFromProgram.keySet()) {
			for (Instance instance : factsFromProgram.get(predicate)) {
				AtomId atomIdFactAtom = atomStore.createAtomId(new PredicateInstance(predicate, instance.terms));
				NoGood noGood = new NoGood(new int[]{-atomIdFactAtom.atomId}, 0);
				// The noGood is assumed to be new.
				int noGoodId = nogoodIdGenerator.getNextId();
				nogoodIdentifiers.put(noGood, noGoodId);
				noGoodsFromFacts.put(noGoodId, noGood);
			}
		}
		for (NonGroundRule nonGroundRule : rulesFromProgram) {
			if (nonGroundRule.isGround()) {
				List<NoGood> noGoods = generateNoGoodsFromGroundSubstitution(nonGroundRule, new VariableSubstitution());
				for (NoGood noGood : noGoods) {
					// Check if noGood was already derived earlier and add it is new
					if (!nogoodIdentifiers.containsKey(noGood)) {
						int noGoodId = nogoodIdGenerator.getNextId();
						nogoodIdentifiers.put(noGood, noGoodId);
						noGoodsFromFacts.put(noGoodId, noGood);
					}
				}
			}
		}
		// TODO: generate noGoods for ground rules! Especially ground rule with empty positive body which yield choices.
		return noGoodsFromFacts;
	}


	@Override
	public Map<Integer, NoGood> getNoGoods() {
		// First call, output all NoGoods from facts.
		if (outputFactNogoods) {
			outputFactNogoods = false;
			return noGoodsFromFacts();
		}
		// Compute new ground rule (evaluate joins with newly changed atoms)
		HashMap<Integer, NoGood> newNoGoods = new HashMap<>();
		for (IndexedInstanceStorage modifiedWorkingMemory : modifiedWorkingMemories) {

			// Iterate over all rules whose body contains the predicate corresponding to the current workingMemory.
			ArrayList<NonGroundRule> nonGroundRules = rulesUsingPredicateWorkingMemory.get(modifiedWorkingMemory);
			// Skip working memories that are not used by any rule.
			if (nonGroundRules == null) {
				continue;
			}
			for (NonGroundRule nonGroundRule : nonGroundRules) {
				List<VariableSubstitution> variableSubstitutions = //generateGroundRulesSemiNaive(nonGroundRule, modifiedWorkingMemory);
				bindNextAtomInRule(nonGroundRule, 0, new VariableSubstitution());
				for (VariableSubstitution variableSubstitution : variableSubstitutions) {
					List<NoGood> noGoods = generateNoGoodsFromGroundSubstitution(nonGroundRule, variableSubstitution);
					for (NoGood noGood : noGoods) {
						// Check if noGood was already derived earlier, add if it is new
						if (!nogoodIdentifiers.containsKey(noGood)) {
							int noGoodId = nogoodIdGenerator.getNextId();
							nogoodIdentifiers.put(noGood, noGoodId);
							newNoGoods.put(noGoodId, noGood);
						}
					}
				}
			}

			// Mark instances added by updateAssignment as done
			modifiedWorkingMemory.markRecentlyAddedInstancesDone();
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
		// Debugging helper: record known grounding substitutions.
		knownGroundingSubstitutions.putIfAbsent(nonGroundRule, new HashSet<>());
		knownGroundingSubstitutions.get(nonGroundRule).add(variableSubstitution);
		// End debugging helper.

		List<NoGood> generatedNoGoods = new ArrayList<>();
		// Collect ground atoms in the body
		ArrayList<AtomId> bodyAtomsPositive = new ArrayList<>();
		ArrayList<AtomId> bodyAtomsNegative = new ArrayList<>();
		for (PredicateInstance predicateInstance : nonGroundRule.bodyAtomsPositive) {
			AtomId groundAtomPositive = SubstitutionUtil.groundingSubstitute(atomStore, predicateInstance, variableSubstitution);
			bodyAtomsPositive.add(groundAtomPositive);
		}
		for (PredicateInstance predicateInstance : nonGroundRule.bodyAtomsNegative) {
			AtomId groundAtomNegative = SubstitutionUtil.groundingSubstitute(atomStore, predicateInstance, variableSubstitution);
			bodyAtomsNegative.add(groundAtomNegative);
		}
		int bodySize = bodyAtomsPositive.size() + bodyAtomsNegative.size();

		if (nonGroundRule.isConstraint()) {
			// A constraint is represented by one NoGood.
			int[] constraintLiterals = new int[bodySize];
			int i = 0;
			for (AtomId atomId : bodyAtomsPositive) {
				constraintLiterals[i++] = atomId.atomId;
			}
			for (AtomId atomId : bodyAtomsNegative) {
				constraintLiterals[i++] = -atomId.atomId;
			}
			NoGood constraintNoGood = new NoGood(constraintLiterals);
			generatedNoGoods.add(constraintNoGood);
		} else {
			// Prepare atom representing the rule body
			PredicateInstance ruleBodyRepresentingPredicate = new PredicateInstance(RULE_BODIES_PREDICATE,
				new Term[]{ConstantTerm.getInstance(Integer.toString(nonGroundRule.getRuleId())),
					ConstantTerm.getInstance(variableSubstitution.toUniformString())});
			// Check uniqueness of ground rule by testing whether the body representing atom already has an id
			if (atomStore.isAtomExisting(ruleBodyRepresentingPredicate)) {
				// The current ground instance already exists, therefore all NoGoods have already been created.
				return generatedNoGoods;
			}
			AtomId bodyRepresentingAtomId = atomStore.createAtomId(ruleBodyRepresentingPredicate);

			// Prepare head atom
			AtomId headAtomId = SubstitutionUtil.groundingSubstitute(atomStore, nonGroundRule.headAtom, variableSubstitution);

			// Create NoGood for body.
			int[] bodyLiterals = new int[bodySize + 1];
			bodyLiterals[0] = -bodyRepresentingAtomId.atomId;
			int i = 1;
			for (AtomId atomId : bodyAtomsPositive) {
				bodyLiterals[i++] = atomId.atomId;
			}
			for (AtomId atomId : bodyAtomsNegative) {
				bodyLiterals[i++] = -atomId.atomId;
			}
			NoGood ruleBody = new NoGood(bodyLiterals, 0);

			// Create NoGood for head.
			NoGood ruleHead = new NoGood(new int[]{-headAtomId.atomId, bodyRepresentingAtomId.atomId}, 0);

			generatedNoGoods.add(ruleBody);
			generatedNoGoods.add(ruleHead);


			// Check if the body of the rule contains negation, add choices then
			if (bodyAtomsNegative.size() != 0) {
				Map<Integer, Integer> newChoiceOn = newChoiceAtoms.getLeft();
				Map<Integer, Integer> newChoiceOff = newChoiceAtoms.getRight();
				// Choice is on the body representing atom

				// ChoiceOn if all positive body atoms are satisfied
				int[] choiceOnLiterals = new int[bodyAtomsPositive.size() + 1];
				i = 1;
				for (AtomId atomId : bodyAtomsPositive) {
					choiceOnLiterals[i++] = atomId.atomId;
				}
				int choiceId = choiceAtomsGenerator.getNextId();
				PredicateInstance choiceOnAtom =  new PredicateInstance(CHOICE_ON_PREDICATE, new Term[]{ConstantTerm.getInstance(Integer.toString(choiceId))});
				int choiceOnAtomIdInt = atomStore.createAtomId(choiceOnAtom).atomId;
				choiceOnLiterals[0] = -choiceOnAtomIdInt;
				// Add corresponding NoGood and ChoiceOn
				generatedNoGoods.add(new NoGood(choiceOnLiterals, 0));	// ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.
				newChoiceOn.put(choiceOnAtomIdInt, bodyRepresentingAtomId.atomId);

				// ChoiceOff if some negative body atom is contradicted
				PredicateInstance choiceOffAtom =  new PredicateInstance(CHOICE_OFF_PREDICATE, new Term[]{ConstantTerm.getInstance(Integer.toString(choiceId))});
				int choiceOffAtomIdInt = atomStore.createAtomId(choiceOffAtom).atomId;
				for (AtomId negAtomId : bodyAtomsNegative) {
					// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
					generatedNoGoods.add(new NoGood(new int[]{-choiceOffAtomIdInt, negAtomId.atomId}, 0));
				}
				newChoiceOff.put(choiceOffAtomIdInt, bodyRepresentingAtomId.atomId);
			}
		}
		return generatedNoGoods;
	}

	class VariableSubstitution {
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
		 * @return
		 */
		public String toUniformString() {
			List<VariableTerm> variablesInSubstitution = new ArrayList<>(substitution.size());
			variablesInSubstitution.addAll(substitution.keySet());
			Collections.sort(variablesInSubstitution); // Hint: Maybe this is a performance issue later, better have sorted/well-defined insertion into VariableSubstitution.
			String ret = "";
			for (VariableTerm variableTerm : variablesInSubstitution) {
				ret += "_" + variableTerm + ":" + substitution.get(variableTerm);
			}
			return ret;
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
	}


	private List<VariableSubstitution> generateGroundRulesSemiNaive(NonGroundRule nonGroundRule, IndexedInstanceStorage modifiedWorkingMemory) {
		ArrayList<VariableSubstitution> generatedSubstitutions = new ArrayList<>();
		bindNextAtom(nonGroundRule, 0, modifiedWorkingMemory.getRecentlyAddedInstances(), new VariableSubstitution(), generatedSubstitutions);
		return generatedSubstitutions;
	}

	private List<VariableSubstitution> bindNextAtomInRule(NonGroundRule rule, int atomPos, VariableSubstitution partialVariableSubstitution) {
		if (atomPos == rule.getNumBodyAtoms()) {
			return Arrays.asList(partialVariableSubstitution);
		} else {
			// check if partialVariableSubstitution already yields a ground atom
			PredicateInstance currentBodyAtom = rule.getBodyAtom(atomPos);
			Pair<Boolean, PredicateInstance> substitute = SubstitutionUtil.substitute(currentBodyAtom, partialVariableSubstitution);
			if (substitute.getLeft()) {
				// substituted atom is ground
				return bindNextAtomInRule(rule, atomPos + 1, partialVariableSubstitution);
			} else {
				// substituted atom contains variables
				ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> wms = workingMemory.get(currentBodyAtom.predicate);
				IndexedInstanceStorage storage = rule.isBodyAtomPositive(atomPos) ? wms.getLeft() : wms.getRight();
				List<Instance> instances;
				if (partialVariableSubstitution.substitution.isEmpty()) {
					// this is the first atom to bind variables, consider only recently added instances.
					instances = storage.getRecentlyAddedInstances();
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
						generatedSubstitutions.addAll(bindNextAtomInRule(rule, atomPos + 1, variableSubstitutionClone));
					}
				}
				return generatedSubstitutions;
			}
		}
	}

	protected void bindNextAtom(NonGroundRule nonGroundRule, int atomPos, List<Instance> potentiallyMatchingInstances, VariableSubstitution variableSubstitution, ArrayList<VariableSubstitution> generatedSubstitutions) {
		PredicateInstance atom = nonGroundRule.getBodyAtom(atomPos);
		// TODO: if variableSubstitution already yields a nonground rule, a valid substitution has been found
		// TODO: for negative body this is sufficient and must yield NoGoods (to guess on).
		// TODO: if current atomPos is inside negative body (and body is sorted such that negatives come last), then the substitution is sufficient (by the rule being safe).
		// TODO: potential solution: skip atoms that are ground given the variableSubstitution, only check potentiallyMatchingInstances for non-ground ones.
		for (Instance instance : potentiallyMatchingInstances) {
			VariableSubstitution variableSubstitutionClone = new VariableSubstitution(variableSubstitution);
			// Check each instance if they match
			if (unify(atom, instance, variableSubstitutionClone)) {
				// If we are at last atom in the body, a full grounding substitution has been derived.
				if (nonGroundRule.getNumBodyAtoms() == atomPos + 1) {
					generatedSubstitutions.add(variableSubstitutionClone);
					continue;	// Check other instances if they also match
				}
				PredicateInstance nextBodyAtom = nonGroundRule.getBodyAtom(atomPos + 1);
				// For selection of the next atom, find ground term to select
				int firstGroundTermIndex = 0;
				Term firstGroundTerm = null;
				for (int i = 0; i < nextBodyAtom.termList.length; i++) {
					Term testTerm = SubstitutionUtil.groundTerm(nextBodyAtom.termList[i], variableSubstitutionClone);
					if (testTerm.isGround()) {
						firstGroundTermIndex = i;
						firstGroundTerm = testTerm;
						break;
					}
				}
				// Get positive or negative memory depending on polarity of the atom
				ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> workingMemory = this.workingMemory.get(nextBodyAtom.predicate);
				IndexedInstanceStorage instanceStorage;
				if (nonGroundRule.isBodyAtomPositive(atomPos + 1)) {
					instanceStorage = workingMemory.getLeft();
				} else {
					instanceStorage = workingMemory.getRight();
				}
				// Select matching instances
				List<Instance> instancesMatchingAtPosition;
				if (firstGroundTerm != null) {
					instancesMatchingAtPosition = instanceStorage.getInstancesMatchingAtPosition(firstGroundTerm, firstGroundTermIndex);
				} else {
					instancesMatchingAtPosition = new ArrayList<>(instanceStorage.getAllInstances());
				}
				bindNextAtom(nonGroundRule, atomPos + 1, instancesMatchingAtPosition, variableSubstitutionClone, generatedSubstitutions);
			}
		}
	}

	/**
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param variableSubstitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	protected boolean unify(PredicateInstance atom, Instance instance, VariableSubstitution variableSubstitution) {
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
			if (ftNonGround.functionSymbol == ftGround.functionSymbol && ftNonGround.termList.size() == ftGround.termList.size()) {
				// Iterate over all subterms of both function terms
				for (int i = 0; i < ftNonGround.termList.size(); i++) {
					if (!unifyTerms(ftNonGround.termList.get(i), ftGround.termList.get(i), variableSubstitution)) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}


	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> currentChoiceAtoms = newChoiceAtoms;
		newChoiceAtoms = new ImmutablePair<>(new HashMap<>(), new HashMap<>());
		return currentChoiceAtoms;
	}

	@Override
	public void updateAssignment(int[] atomIds, boolean[] truthValues) {
		for (int i = 0; i < atomIds.length; i++) {
			AtomId atomId = new AtomId(atomIds[i]);
			PredicateInstance predicateInstance = atomStore.getPredicateInstance(atomId);
			Instance instance = new Instance(predicateInstance.termList);
			boolean truthValue = truthValues[i];
			ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> workingMemoryPlusMinus = workingMemory.get(predicateInstance.predicate);
			IndexedInstanceStorage workingMemoryPlus = workingMemoryPlusMinus.getLeft();
			IndexedInstanceStorage workingMemoryMinus = workingMemoryPlusMinus.getRight();
			if (truthValue) {
				workingMemoryPlus.addInstance(instance);
				modifiedWorkingMemories.add(workingMemoryPlus);
			} else {
				workingMemoryMinus.addInstance(instance);
				modifiedWorkingMemories.add(workingMemoryMinus);
			}
		}
	}

	@Override
	public void forgetAssignment(int[] atomIds) {

	}

	@Override
	public String atomIdToString(int atomId) {
		return atomStore.getPredicateInstance(new AtomId(atomId)).toString();
	}

	private HashMap<NonGroundRule, HashSet<VariableSubstitution>> knownGroundingSubstitutions = new HashMap<>();
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
}
