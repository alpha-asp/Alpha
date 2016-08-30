package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.NoGood;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFact;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
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

	protected BidiMap<NoGood, Integer> nogoodIdentifiers = new DualHashBidiMap<>();
	private IntIdGenerator nogoodIdGenerator = new IntIdGenerator();

	private HashMap<Predicate, ArrayList<Instance>> factsFromProgram = new HashMap<>();
	private boolean outputFactNogoods = true;

	protected AtomStore atomStore = new AtomStore();

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
				termList.add(atomStore.convertFromParsedTerm(fact.fact.terms.get(i)));
			}
			Instance instance = new Instance(termList.toArray(new Term[0]));
			// Add instance to corresponding list of facts
			factsFromProgram.putIfAbsent(predicate, new ArrayList<>());
			ArrayList<Instance> internalPredicateInstances = factsFromProgram.get(predicate);
			internalPredicateInstances.add(instance);
		}
		// initialize rules
		// initialize constraints
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
		if (outputFactNogoods) {
			outputFactNogoods = false;
			return noGoodsFromFacts();
		}
		// TODO: handle cases where some ground rule instance can be derived.
		return new HashMap<>();
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		return new ImmutablePair<>(new HashMap<>(), new HashMap<>()); // return pair of empty maps
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
			} else {
				workingMemoryMinus.addInstance(instance);
			}
		}
	}

	@Override
	public void forgetAssignment(int[] atomIds) {

	}
}
