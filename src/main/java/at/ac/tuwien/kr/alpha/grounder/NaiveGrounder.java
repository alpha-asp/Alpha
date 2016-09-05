package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.NoGood;
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

	protected BidiMap<NoGood, Integer> nogoodIdentifiers = new DualHashBidiMap<>();
	private IntIdGenerator nogoodIdGenerator = new IntIdGenerator();

	private HashMap<Predicate, ArrayList<Instance>> factsFromProgram = new HashMap<>();
	private boolean outputFactNogoods = true;

	private ArrayList<NonGroundRule> rulesFromProgram = new ArrayList<>();

	protected AtomStore atomStore = new AtomStore();

	private ArrayList<IndexedInstanceStorage> modifiedWorkingMemories = new ArrayList<>();
	private HashMap<IndexedInstanceStorage, ArrayList<NonGroundRule>> rulesUsingPredicateWorkingMemory = new HashMap<>();

	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new HashMap<>(), new HashMap<>());
	private IntIdGenerator choiceAtomsGenerator = new IntIdGenerator();

	// TODO: make each rule having its own object where its representation is stored.
	// TODO: add a set containing all joins that are somewhere computed.

	private HashSet<Predicate> knownPredicates = new HashSet<>();

	public NaiveGrounder(ParsedProgram program) {
		super(program);
		// initialize all facts
		for (ParsedFact fact : this.program.facts) {
			String predicateName = fact.fact.predicate;
			int predicateArity = fact.fact.arity;
			Predicate predicate = new BasicPredicate(predicateName, predicateArity);
			// Record predicate
			knownPredicates.add(predicate);

			// Create working memory for predicate if it does not exist
			if (!workingMemory.containsKey(predicate)) {
				workingMemory.put(predicate, new ImmutablePair<>(new IndexedInstanceStorage(predicateName + "+", predicateArity),
					new IndexedInstanceStorage(predicateName + "-", predicateArity)));
			}
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

	private void registerRuleOrConstraint(ParsedRule rule) {
		// Record the rule for later use
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(rule);
		rulesFromProgram.add(nonGroundRule);
		// Register the rule at all working memories corresponding to a predicate used in the body of the rule.
		for (Predicate predicate : nonGroundRule.usedPositiveBodyPredicates()) {
			IndexedInstanceStorage workingMemoryPlus = workingMemory.get(predicate).getLeft();
			rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemoryPlus, new ArrayList<>());
			ArrayList<NonGroundRule> nonGroundRules = rulesUsingPredicateWorkingMemory.get(workingMemoryPlus);
			nonGroundRules.add(nonGroundRule);
		}
		for (Predicate predicate : nonGroundRule.usedNegativeBodyPredicates()) {
			IndexedInstanceStorage workingMemoryMinus = workingMemory.get(predicate).getRight();
			rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemoryMinus, new ArrayList<>());
			ArrayList<NonGroundRule> nonGroundRules = rulesUsingPredicateWorkingMemory.get(workingMemoryMinus);
			nonGroundRules.add(nonGroundRule);
		}
	}

	@Override
	public AnswerSet assignmentToAnswerSet(java.util.function.Predicate<Predicate> filter, int[] trueAtoms) {

		HashMap<Predicate, ArrayList<PredicateInstance>> predicateInstances = new HashMap<>();
		HashSet<Predicate> knownPredicates = new HashSet<>();

		// Iterate over all true atomIds, get instances from atomStore and add them if not filtered.
		for (int trueAtom : trueAtoms) {
			PredicateInstance predicateInstance = atomStore.getPredicateInstance(new AtomId(trueAtom));
			// Skip filtered predicates.
			if (!filter.test(predicateInstance.predicate)) {
				continue;
			}

			knownPredicates.add(predicateInstance.predicate);
			predicateInstances.putIfAbsent(predicateInstance.predicate, new ArrayList<>());
			ArrayList<PredicateInstance> instances = predicateInstances.get(predicateInstance.predicate);
			instances.add(predicateInstance);
		}
		ArrayList<Predicate> predicateList = new ArrayList<>();
		predicateList.addAll(knownPredicates);

		BasicAnswerSet answerSet = new BasicAnswerSet();
		answerSet.setPredicateList(predicateList);
		answerSet.setPredicateInstances(predicateInstances);

		return answerSet;
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
				// TODO: handle cases where some ground rule instance can be derived.
				List<VariableSubstitution> variableSubstitutions = generateGroundRulesSemiNaive(nonGroundRule, modifiedWorkingMemory);
				for (VariableSubstitution variableSubstitution : variableSubstitutions) {
					List<NoGood> noGoods = generateNoGoodsFromGroundSubstitution(nonGroundRule, variableSubstitution);
					for (NoGood noGood : noGoods) {
						// Check if noGood was already derived earlier and add it is new
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
		modifiedWorkingMemories = new ArrayList<>();
		return newNoGoods;
	}

	/**
	 * Generates all NoGoods resulting from a non-ground rule and a variable substitution.
	 * @param nonGroundRule
	 * @param variableSubstitution
	 * @return
	 */
	private List<NoGood> generateNoGoodsFromGroundSubstitution(NonGroundRule nonGroundRule, VariableSubstitution variableSubstitution) {
		List<NoGood> generatedNoGoods = new ArrayList<>();

		// Collect ground atoms in the body
		ArrayList<AtomId> bodyAtoms = new ArrayList<>();
		ArrayList<AtomId> bodyAtomsPositive = new ArrayList<>();
		ArrayList<AtomId> bodyAtomsNegative = new ArrayList<>();
		for (PredicateInstance predicateInstance : nonGroundRule.bodyAtomsPositive) {
			AtomId groundAtomPositive = SubstitutionUtil.substitute(atomStore, predicateInstance, variableSubstitution);
			bodyAtomsPositive.add(groundAtomPositive);
			bodyAtoms.add(groundAtomPositive);
		}
		for (PredicateInstance predicateInstance : nonGroundRule.bodyAtomsNegative) {
			AtomId groundAtomNegative = SubstitutionUtil.substitute(atomStore, predicateInstance, variableSubstitution);
			bodyAtomsNegative.add(groundAtomNegative);
			bodyAtoms.add(groundAtomNegative);
		}

		if (nonGroundRule.isConstraint()) {
			// A constraint is represented by one NoGood.
			int[] constraintLiterals = new int[bodyAtoms.size()];
			int i = 0;
			for (AtomId atomId : bodyAtoms) {
				constraintLiterals[i++] = atomId.atomId;
			}
			NoGood constraintNoGood = new NoGood(constraintLiterals);
			generatedNoGoods.add(constraintNoGood);
		} else {
			// Prepare atom representing the rule body
			PredicateInstance ruleBodyRepresentingPredicate = new PredicateInstance(RULE_BODIES_PREDICATE,
				new Term[]{ConstantTerm.getConstantTerm(Integer.toString(nonGroundRule.getRuleId())),
					ConstantTerm.getConstantTerm(variableSubstitution.toUniformString())});
			// Check uniqueness of ground rule by testing whether the body representing atom already has an id
			if (atomStore.isAtomExisting(ruleBodyRepresentingPredicate)) {
				// The current ground instance already exists, therefore all NoGoods have already been created.
				return generatedNoGoods;
			}
			AtomId bodyRepresentingAtomId = atomStore.createAtomId(ruleBodyRepresentingPredicate);

			// Prepare head atom
			AtomId headAtomId = SubstitutionUtil.substitute(atomStore, nonGroundRule.headAtom, variableSubstitution);

			// Create NoGood for body.
			int[] bodyLiterals = new int[bodyAtoms.size() + 1];
			bodyLiterals[0] = -bodyRepresentingAtomId.atomId;
			int i = 1;
			for (AtomId atomId : bodyAtoms) {
				bodyLiterals[i++] = atomId.atomId;
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
				int choiceOnAtomIdInt = choiceAtomsGenerator.getNextId();
				choiceOnLiterals[0] = -choiceOnAtomIdInt;
				// Add corresponding NoGood and ChoiceOn
				generatedNoGoods.add(new NoGood(choiceOnLiterals));
				newChoiceOn.put(choiceOnAtomIdInt, bodyRepresentingAtomId.atomId);

				// ChoiceOff if some negative body atom is contradicted
				int choiceOffAtomIdInt = choiceAtomsGenerator.getNextId();
				for (AtomId negAtomId : bodyAtomsNegative) {
					generatedNoGoods.add(new NoGood(new int[]{-choiceOffAtomIdInt, negAtomId.atomId}));
				}
				newChoiceOff.put(choiceOffAtomIdInt, bodyRepresentingAtomId.atomId);
			}
		}
		return generatedNoGoods;
	}

	class VariableSubstitution {
		HashMap<VariableTerm, Term> substitution;

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
	}


	private List<VariableSubstitution> generateGroundRulesSemiNaive(NonGroundRule nonGroundRule, IndexedInstanceStorage modifiedWorkingMemory) {
		ArrayList<VariableSubstitution> generatedSubstitutions = new ArrayList<>();
		// TODO: semi-naive grounding here!
		return generatedSubstitutions;
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
}
