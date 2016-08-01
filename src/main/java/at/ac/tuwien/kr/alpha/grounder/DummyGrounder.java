package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.NoGood;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.entriesToMap;
import static at.ac.tuwien.kr.alpha.Util.entry;

/**
 * Represents a small ASP program {@code { c :- a, b.  a.  b. }}.
 *
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummyGrounder extends AbstractGrounder {
	private static final Log LOG = LogFactory.getLog(AbstractGrounder.class);

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
	public AnswerSet assignmentToAnswerSet(java.util.function.Predicate<Predicate> filter, int[] trueAtoms) {
		// Note: This grounder only deals with 0-ary predicates, i.e., every atom is a predicate and there is
		// 	 only one predicate instance representing 0 terms.
		BasicAnswerSet answerSet = new BasicAnswerSet();

		ArrayList<Predicate> trueAtomPredicates = new ArrayList<>();
		for (int trueAtom : trueAtoms) {
			BasicPredicate atomPredicate = new BasicPredicate(atomIdToString.get(trueAtom), 0);
			if (filter.test(atomPredicate)) {
				trueAtomPredicates.add(atomPredicate);
			}
		}

		answerSet.setPredicateList(trueAtomPredicates);


		// Add the 0-ary instance (it is the only one for this grounder)
		HashMap<Integer, String> termIdStringMap = new HashMap<>();
		termIdStringMap.put(0, "");
		answerSet.setTermIdStringMap(termIdStringMap);

		// Add the atom instances
		HashMap<Predicate, ArrayList<PredicateInstance>> predicateInstances = new HashMap<>();
		for (Predicate trueAtomPredicate : trueAtomPredicates) {
			PredicateInstance predicateInstance = new PredicateInstance();
			predicateInstance.termList = new LinkedList<>();
			predicateInstance.predicate = trueAtomPredicate;
			ArrayList<PredicateInstance> instanceList = new ArrayList<>();
			instanceList.add(predicateInstance);
			predicateInstances.put(trueAtomPredicate, instanceList);
		}
		answerSet.setPredicateInstances(predicateInstances);

		LOG.debug(
			// NOTE(flowlo): If this stream would map to Predicate
			// it could be easily filtered by filter.
			Arrays.stream(trueAtoms)
				.mapToObj(atomIdToString::get)
				.collect(Collectors.joining(", ", "{ ", " }"))
		);
		return answerSet;
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
