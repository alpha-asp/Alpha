package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.entriesToMap;
import static at.ac.tuwien.kr.alpha.Util.entry;

/**
 * Represents a small ASP program {@code { c :- a, b.  a.  b. }}.
 *
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummyGrounder implements Grounder {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGrounder.class);

	private static Map<Integer, String> atomIdToString = Stream.of(
		entry(1, "a"),
		entry(2, "b"),
		entry(3, "_br1"),
		entry(4, "c")
	).collect(entriesToMap());

	private static final int FACT_A = 11; // { -a }
	private static final int FACT_B = 12; // { -b }
	private static final int RULE_B = 13; // { -_br1, a, b }
	private static final int RULE_H = 14; // { -c, _br1 }

	private static final Map<Integer, NoGood> NOGOODS = Stream.of(
		entry(FACT_A, new NoGood(new int[]{-1 }, 0)),
		entry(FACT_B, new NoGood(new int[]{-2 }, 0)),
		entry(RULE_B, new NoGood(new int[]{-3, 1, 2 }, 0)),
		entry(RULE_H, new NoGood(new int[]{-4, 3 }, 0))
	).collect(entriesToMap());

	private byte[] currentTruthValues = new byte[]{-2, -1, -1, -1, -1};
	private Set<Integer> returnedNogoods = new HashSet<>();

	@Override
	public void updateAssignment(int[] atomIds, boolean[] truthValues) {
		for (int i = 0; i < atomIds.length; i++) {
			currentTruthValues[atomIds[i]] = truthValues[i] ? (byte)1 : (byte)0;
		}
	}

	@Override
	public void forgetAssignment(int[] atomIds) {
		for (int atomId : atomIds) {
			currentTruthValues[atomId] = -1;
		}
	}

	@Override
	public String atomIdToString(int atomId) {
		return Integer.toString(atomId);
	}

	@Override
	public AnswerSet assignmentToAnswerSet(java.util.function.Predicate<Predicate> filter, Iterable<Integer> trueAtoms) {
		// Note: This grounder only deals with 0-ary predicates, i.e., every atom is a predicate and there is
		// 	 only one predicate instance representing 0 terms.

		Set<Predicate> trueAtomPredicates = new HashSet<>();
		for (int trueAtom : trueAtoms) {
			BasicPredicate atomPredicate = new BasicPredicate(atomIdToString.get(trueAtom), 0);
			if (filter.test(atomPredicate)) {
				trueAtomPredicates.add(atomPredicate);
			}
		}

		// Add the atom instances
		Map<Predicate, Set<PredicateInstance>> predicateInstances = new HashMap<>();
		for (Predicate trueAtomPredicate : trueAtomPredicates) {
			PredicateInstance internalPredicateInstance = new PredicateInstance(trueAtomPredicate);
			Set<PredicateInstance> instanceList = new HashSet<>();
			instanceList.add(internalPredicateInstance);
			predicateInstances.put(trueAtomPredicate, instanceList);
		}

		return  new BasicAnswerSet(trueAtomPredicates, predicateInstances);
	}

	@Override
	public Map<Integer, NoGood> getNoGoods() {
		// Return NoGoods depending on current assignment.
		HashMap<Integer, NoGood> returnNoGoods = new HashMap<>();
		if (currentTruthValues[1] == 1 && currentTruthValues[2] == 1) {
			addNoGoodIfNotAlreadyReturned(returnNoGoods, RULE_B);
			addNoGoodIfNotAlreadyReturned(returnNoGoods, RULE_H);
		} else {
			addNoGoodIfNotAlreadyReturned(returnNoGoods, FACT_A);
			addNoGoodIfNotAlreadyReturned(returnNoGoods, FACT_B);
		}
		return returnNoGoods;
	}

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		return new ImmutablePair<>(new HashMap<>(), new HashMap<>());
	}

	private void addNoGoodIfNotAlreadyReturned(Map<Integer, NoGood> integerNoGoodMap, Integer idNoGood) {
		if (!returnedNogoods.contains(idNoGood)) {
			integerNoGoodMap.put(idNoGood, NOGOODS.get(idNoGood));
			returnedNogoods.add(idNoGood);
		}
	}
}
