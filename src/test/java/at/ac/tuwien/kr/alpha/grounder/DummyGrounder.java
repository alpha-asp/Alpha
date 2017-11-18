package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.entriesToMap;
import static at.ac.tuwien.kr.alpha.Util.entry;
import static at.ac.tuwien.kr.alpha.common.NoGood.headFirst;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

/**
 * Represents a small ASP program {@code { c :- a, b.  a.  b. }}.
 *
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummyGrounder implements Grounder {
	public static final Set<AnswerSet> EXPECTED = new HashSet<>(singletonList(new AnswerSetBuilder()
		.predicate("a")
		.predicate("b")
		.predicate("c")
		.build()
	));
	private static final int FACT_A = 11; // { -a }
	private static final int FACT_B = 12; // { -b }
	private static final int RULE_B = 13; // { -_br1, a, b }
	private static final int RULE_H = 14; // { -c, _br1 }
	private static final Map<Integer, NoGood> NOGOODS = Stream.of(
		entry(FACT_A, headFirst(-1)),
		entry(FACT_B, headFirst(-2)),
		entry(RULE_B, headFirst(-3, 1, 2)),
		entry(RULE_H, headFirst(-4, 3))
	).collect(entriesToMap());
	private static Map<Integer, String> atomIdToString = Stream.of(
		entry(1, "a"),
		entry(2, "b"),
		entry(3, "_br1"),
		entry(4, "c")
	).collect(entriesToMap());
	private final java.util.function.Predicate<Predicate> filter;
	private byte[] currentTruthValues = new byte[]{-2, -1, -1, -1, -1};
	private Set<Integer> returnedNogoods = new HashSet<>();

	public DummyGrounder() {
		this(p -> true);
	}

	public DummyGrounder(java.util.function.Predicate<Predicate> filter) {
		this.filter = filter;
	}

	@Override
	public void forgetAssignment(int[] atomIds) {
		for (int atomId : atomIds) {
			currentTruthValues[atomId] = -1;
		}
	}

	@Override
	public String atomToString(int atomId) {
		return Integer.toString(atomId);
	}

	@Override
	public List<Integer> getUnassignedAtoms(Assignment assignment) {
		List<Integer> unassigned = new ArrayList<>();
		List<Integer> knownAtomIds = Arrays.asList(1, 2, 3, 4);
		for (Integer atomId : knownAtomIds) {
			if (!assignment.isAssigned(atomId)) {
				unassigned.add(atomId);
			}
		}
		return unassigned;
	}

	private int solverDerivedNoGoodIdCounter = 20;
	private Map<NoGood, Integer> solverDerivedNoGoods = new HashMap<>();

	@Override
	public int registerOutsideNoGood(NoGood noGood) {
		if (!solverDerivedNoGoods.containsKey(noGood)) {
			solverDerivedNoGoods.put(noGood, solverDerivedNoGoodIdCounter++);
		}
		return solverDerivedNoGoods.get(noGood);
	}

	@Override
	public boolean isAtomChoicePoint(int atom) {
		// No choice points here.
		return false;
	}

	@Override
	public int getMaxAtomId() {
		return 14;
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		// Note: This grounder only deals with 0-ary predicates, i.e., every atom is a predicate and there is
		// 	 only one predicate instance representing 0 terms.

		SortedSet<Predicate> trueAtomPredicates = new TreeSet<>();
		for (int trueAtom : trueAtoms) {
			Predicate atomPredicate = new Predicate(atomIdToString.get(trueAtom), 0);
			if (!filter.test(atomPredicate)) {
				continue;
			}
			if ("_br1".equals(atomPredicate.getPredicateName())) {
				continue;
			}
			trueAtomPredicates.add(atomPredicate);
		}

		// Add the atom instances
		Map<Predicate, SortedSet<Atom>> predicateInstances = new HashMap<>();
		for (Predicate trueAtomPredicate : trueAtomPredicates) {
			BasicAtom internalBasicAtom = new BasicAtom(trueAtomPredicate);
			predicateInstances.put(trueAtomPredicate, new TreeSet<>(singleton(internalBasicAtom)));
		}

		return new BasicAnswerSet(trueAtomPredicates, predicateInstances);
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(Assignment assignment) {
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

	@Override
	public void updateAssignment(Iterator<Assignment.Entry> it) {
		while (it.hasNext()) {
			Assignment.Entry assignment = it.next();
			Truth truthValue = assignment.getTruth();
			currentTruthValues[assignment.getAtom()] = (byte)(truthValue.toBoolean() ? 1 : 0);
		}
	}

	private void addNoGoodIfNotAlreadyReturned(Map<Integer, NoGood> integerNoGoodMap, Integer idNoGood) {
		if (!returnedNogoods.contains(idNoGood)) {
			integerNoGoodMap.put(idNoGood, NOGOODS.get(idNoGood));
			returnedNogoods.add(idNoGood);
		}
	}
}
