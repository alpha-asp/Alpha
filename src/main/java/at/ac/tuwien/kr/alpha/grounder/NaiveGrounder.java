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
import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.*;
import at.ac.tuwien.kr.alpha.solver.Assignment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounder extends AbstractGrounder {
	private static final Logger LOGGER = LoggerFactory.getLogger(NaiveGrounder.class);

	protected HashMap<Predicate, ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage>> workingMemory = new HashMap<>();
	protected Map<NoGood, Integer> nogoodIdentifiers = new LinkedHashMap<>();
	protected AtomStore atomStore = new AtomStore();

	private final IntIdGenerator intIdGenerator = new IntIdGenerator();
	private IntIdGenerator nogoodIdGenerator = new IntIdGenerator();
	private IntIdGenerator choiceAtomsGenerator = new IntIdGenerator();

	private HashMap<Predicate, ArrayList<Instance>> factsFromProgram = new LinkedHashMap<>();
	private boolean outputFactNogoods = true;
	private ArrayList<NonGroundRule> rulesFromProgram = new ArrayList<>();
	private HashSet<IndexedInstanceStorage> modifiedWorkingMemories = new LinkedHashSet<>();
	private HashMap<IndexedInstanceStorage, ArrayList<FirstBindingAtom>> rulesUsingPredicateWorkingMemory = new HashMap<>();
	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
	private HashSet<Predicate> knownPredicates = new HashSet<>();
	private HashMap<NonGroundRule, HashSet<Substitution>> knownGroundingSubstitutions = new LinkedHashMap<>();

	public NaiveGrounder(ParsedProgram program) {
		this(program, p -> true);
	}

	public NaiveGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter) {
		super(program, filter);

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
		adaptWorkingMemoryForPredicate(RuleAtom.PREDICATE);
		adaptWorkingMemoryForPredicate(ChoiceAtom.OFF);
		adaptWorkingMemoryForPredicate(ChoiceAtom.ON);
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
		HashSet<Atom> registeredPositiveAtoms = new HashSet<>();
		for (int i = 0; i < nonGroundRule.getBodyAtomsPositive().size(); i++) {
			registerAtomAtWorkingMemory(true, nonGroundRule, registeredPositiveAtoms, i);
		}
		// Register negative literals only if the rule contains no positive literals (necessary grounding is ensured by safety of rules).
		if (nonGroundRule.getBodyAtomsPositive().isEmpty()) {
			HashSet<Atom> registeredNegativeAtoms = new HashSet<>();
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
	private void registerAtomAtWorkingMemory(boolean isPositive, NonGroundRule nonGroundRule, HashSet<Atom> registeredAtoms, int atomPos) {
		Atom bodyAtom = isPositive ? nonGroundRule.getBodyAtomsPositive().get(atomPos) : nonGroundRule.getBodyAtomsNegative().get(atomPos);
		if (registeredAtoms.contains(bodyAtom)) {
			return;
		}
		Predicate predicate = bodyAtom.getPredicate();
		registeredAtoms.add(bodyAtom);
		IndexedInstanceStorage workingMemory = isPositive ? this.workingMemory.get(predicate).getLeft() : this.workingMemory.get(predicate).getRight();
		rulesUsingPredicateWorkingMemory.putIfAbsent(workingMemory, new ArrayList<>());
		rulesUsingPredicateWorkingMemory.get(workingMemory).add(new FirstBindingAtom(nonGroundRule, atomPos, bodyAtom));
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		Map<Predicate, SortedSet<Atom>> predicateInstances = new LinkedHashMap<>();
		SortedSet<Predicate> knownPredicates = new TreeSet<>();

		if (!trueAtoms.iterator().hasNext()) {
			return BasicAnswerSet.EMPTY;
		}

		// Iterate over all true atomIds, computeNextAnswerSet instances from atomStore and add them if not filtered.
		for (int trueAtom : trueAtoms) {
			final Atom atom = atomStore.get(trueAtom);

			// Skip internal predicates.
			if (atom.isInternal()) {
				continue;
			}

			final Predicate predicate = atom.getPredicate();

			// Skip filtered predicates.
			if (!filter.test(predicate)) {
				continue;
			}

			knownPredicates.add(predicate);
			predicateInstances.putIfAbsent(predicate, new TreeSet<>());
			Set<Atom> instances = predicateInstances.get(predicate);
			instances.add(atom);
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
		HashMap<Integer, NoGood> noGoodsFromFacts = new LinkedHashMap<>();
		for (Predicate predicate : factsFromProgram.keySet()) {
			for (Instance instance : factsFromProgram.get(predicate)) {
				NoGood noGood = NoGood.headFirst(-atomStore.add(new BasicAtom(predicate, instance.terms)));
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
			register(generateNoGoodsFromGroundSubstitution(nonGroundRule, new Substitution()), noGoodsFromFacts);
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
		HashMap<Integer, NoGood> newNoGoods = new LinkedHashMap<>();
		for (IndexedInstanceStorage modifiedWorkingMemory : modifiedWorkingMemories) {
			// Iterate over all rules whose body contains the predicate corresponding to the current workingMemory.
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
					Substitution partialSubstitution = new Substitution();
					if (unify(firstBindingAtom.firstBindingAtom, instance, partialSubstitution)) {
						substitutions.addAll(bindNextAtomInRule(nonGroundRule, 0, firstBindingAtom.firstBindingAtomPos, partialSubstitution));
					}
				}
				for (Substitution substitution : substitutions) {
					register(generateNoGoodsFromGroundSubstitution(nonGroundRule, substitution), newNoGoods);
				}
			}

			// Mark instances added by updateAssignment as done
			modifiedWorkingMemory.markRecentlyAddedInstancesDone();
		}
		modifiedWorkingMemories = new LinkedHashSet<>();
		return newNoGoods;
	}

	private void register(Iterable<NoGood> noGoods, Map<Integer, NoGood> difference) {
		for (NoGood noGood : noGoods) {
			// Check if noGood was already derived earlier, add if it is new
			if (!nogoodIdentifiers.containsKey(noGood)) {
				int noGoodId = nogoodIdGenerator.getNextId();
				nogoodIdentifiers.put(noGood, noGoodId);
				difference.put(noGoodId, noGood);
			}
		}
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

		List<NoGood> generatedNoGoods = new ArrayList<>();
		// Collect ground atoms in the body
		ArrayList<Integer> bodyAtomsPositive = new ArrayList<>();
		ArrayList<Integer> bodyAtomsNegative = new ArrayList<>();
		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom instanceof BuiltinAtom) {
				// Truth of builtin atoms does not depend on any assignment
				// hence, they need not be represented as long as they evaluate to true
				if (((BuiltinAtom) atom).evaluate(substitution)) {
					continue;
				} else {
					// Rule body is always false, skip the whole rule.
					return new ArrayList<>(0);
				}
			}
			int groundAtomPositive = atomStore.add(atom.substitute(substitution));
			bodyAtomsPositive.add(groundAtomPositive);
		}
		for (Atom atom : nonGroundRule.getBodyAtomsNegative()) {
			int groundAtomNegative = atomStore.add(atom.substitute(substitution));
			bodyAtomsNegative.add(groundAtomNegative);
		}
		int bodySize = bodyAtomsPositive.size() + bodyAtomsNegative.size();

		if (nonGroundRule.isConstraint()) {
			// A constraint is represented by one NoGood.
			int[] constraintLiterals = new int[bodySize];
			int i = 0;
			for (Integer atomId : bodyAtomsPositive) {
				constraintLiterals[i++] = +atomId;
			}
			for (Integer atomId : bodyAtomsNegative) {
				constraintLiterals[i++] = -atomId;
			}
			NoGood constraintNoGood = new NoGood(constraintLiterals);
			generatedNoGoods.add(constraintNoGood);
		} else {
			// Prepare atom representing the rule body
			Atom ruleBodyRepresentingPredicate = new RuleAtom(nonGroundRule, substitution);
			// Check uniqueness of ground rule by testing whether the body representing atom already has an id
			if (atomStore.contains(ruleBodyRepresentingPredicate)) {
				// The current ground instance already exists, therefore all NoGoods have already been created.
				return generatedNoGoods;
			}
			int bodyRepresentingAtomId = atomStore.add(ruleBodyRepresentingPredicate);

			// Prepare head atom
			int headAtomId = atomStore.add(nonGroundRule.getHeadAtom().substitute(substitution));

			// Create NoGood for body.
			int[] bodyLiterals = new int[bodySize + 1];
			bodyLiterals[0] = -bodyRepresentingAtomId;
			int i = 1;
			for (Integer atomId : bodyAtomsPositive) {
				bodyLiterals[i++] = atomId;
			}
			for (Integer atomId : bodyAtomsNegative) {
				bodyLiterals[i++] = -atomId;
			}
			NoGood ruleBody = NoGood.headFirst(bodyLiterals);

			// Generate NoGoods such that the atom representing the body is true iff the body is true.
			for (int j = 1; j < bodyLiterals.length; j++) {
				generatedNoGoods.add(new NoGood(bodyRepresentingAtomId, -bodyLiterals[j]));
			}

			// Create NoGood for head.
			NoGood ruleHead = NoGood.headFirst(-headAtomId, bodyRepresentingAtomId);

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
				for (Integer atomId : bodyAtomsPositive) {
					choiceOnLiterals[i++] = atomId;
				}
				int choiceId = choiceAtomsGenerator.getNextId();
				Atom choiceOnAtom = ChoiceAtom.on(choiceId);
				int choiceOnAtomIdInt = atomStore.add(choiceOnAtom);
				choiceOnLiterals[0] = -choiceOnAtomIdInt;
				// Add corresponding NoGood and ChoiceOn
				generatedNoGoods.add(NoGood.headFirst(choiceOnLiterals));	// ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.
				newChoiceOn.put(bodyRepresentingAtomId, choiceOnAtomIdInt);

				// ChoiceOff if some negative body atom is contradicted
				Atom choiceOffAtom = ChoiceAtom.off(choiceId);
				int choiceOffAtomIdInt = atomStore.add(choiceOffAtom);
				for (Integer negAtomId : bodyAtomsNegative) {
					// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
					generatedNoGoods.add(NoGood.headFirst(-choiceOffAtomIdInt, negAtomId));
				}
				newChoiceOff.put(bodyRepresentingAtomId, choiceOffAtomIdInt);
			}
		}
		return generatedNoGoods;
	}

	private List<Substitution> bindNextAtomInRule(NonGroundRule rule, int atomPos, int firstBindingPos, Substitution partialSubstitution) {
		if (atomPos == rule.getNumBodyAtoms()) {
			return Collections.singletonList(partialSubstitution);
		}

		if (atomPos == firstBindingPos) {
			// Binding for this position was already computed, skip it.
			return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution);
		}

		Atom currentAtom = rule.getBodyAtom(atomPos);
		if (currentAtom instanceof BuiltinAtom) {
			// Assumption: all variables occurring in the builtin atom are already bound
			// (as ensured by the body atom sorting)
			if (((BuiltinAtom)currentAtom).evaluate(partialSubstitution)) {
				// Builtin is true, continue with next atom in rule body.
				return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution);
			}

			// Builtin is false, return no bindings.
			return Collections.emptyList();
		}

		// check if partialSubstitution already yields a ground atom
		Atom substitute = currentAtom.substitute(partialSubstitution);
		if (substitute.isGround()) {
			// Substituted atom is ground, in case it is positive, only ground if it also holds true
			if (rule.isBodyAtomPositive(atomPos)) {
				IndexedInstanceStorage wm = workingMemory.get(currentAtom.getPredicate()).getLeft();
				if (wm.containsInstance(new Instance(substitute.getTerms()))) {
					// Ground literal holds, continue finding a variable substitution.
					return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution);
				}

				// Generate no variable substitution.
				return Collections.emptyList();
			}

			// Atom occurs negated in the rule, continue grounding
			return bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, partialSubstitution);
		}

		// substituted atom contains variables
		ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> wms = workingMemory.get(currentAtom.getPredicate());
		IndexedInstanceStorage storage = rule.isBodyAtomPositive(atomPos) ? wms.getLeft() : wms.getRight();
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
			Substitution substitutionClone = new Substitution(partialSubstitution);
			if (unify(substitute, instance, substitutionClone)) {
				generatedSubstitutions.addAll(bindNextAtomInRule(rule, atomPos + 1, firstBindingPos, substitutionClone));
			}
		}

		return generatedSubstitutions;
	}

	/**
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param substitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	protected boolean unify(Atom atom, Instance instance, Substitution substitution) {
		Substitution tempSubstitution = new Substitution(substitution);
		for (int i = 0; i < instance.terms.size(); i++) {
			if (instance.terms.get(i) == atom.getTerms().get(i) ||
				tempSubstitution.unifyTerms(atom.getTerms().get(i), instance.terms.get(i))) {
				continue;
			}
			return false;
		}
		substitution.replaceSubstitution(tempSubstitution);
		return true;
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
	public String atomToString(int atomId) {
		return atomStore.get(atomId).toString();
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
		if (!nogoodIdentifiers.containsKey(noGood)) {
			int noGoodId = nogoodIdGenerator.getNextId();
			nogoodIdentifiers.put(noGood, noGoodId);
			return noGoodId;
		}
		return nogoodIdentifiers.get(noGood);
	}

	@Override
	public boolean isAtomChoicePoint(int atom) {
		return atomStore.get(atom) instanceof RuleAtom;
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

	public void printCurrentlyKnownNoGoods() {
		System.out.println("Printing known NoGoods:");
		for (Map.Entry<NoGood, Integer> noGoodEntry : nogoodIdentifiers.entrySet()) {
			System.out.println(noGoodEntry.getKey().toStringReadable(this));
		}
	}

	public static String groundAndPrintRule(NonGroundRule rule, Substitution substitution) {
		String ret = "";
		if (!rule.isConstraint()) {
			Atom groundHead = rule.getHeadAtom().substitute(substitution);
			ret += groundHead.toString();
		}
		ret += " :- ";
		boolean isFirst = true;
		for (Atom bodyAtom : rule.getBodyAtomsPositive()) {
			ret += groundAtomToString(bodyAtom, false, substitution, isFirst);
			isFirst = false;
		}
		for (Atom bodyAtom : rule.getBodyAtomsNegative()) {
			ret += groundAtomToString(bodyAtom, true, substitution, isFirst);
			isFirst = false;
		}
		ret += ".";
		return ret;
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
