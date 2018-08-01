package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.entriesToMap;
import static at.ac.tuwien.kr.alpha.Util.entry;
import static at.ac.tuwien.kr.alpha.common.NoGoodTest.fromOldLiterals;
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
		entry(FACT_A, headFirst(fromOldLiterals(-1))),
		entry(FACT_B, headFirst(fromOldLiterals(-2))),
		entry(RULE_B, headFirst(fromOldLiterals(-3, 1, 2))),
		entry(RULE_H, headFirst(fromOldLiterals(-4, 3)))
	).collect(entriesToMap());
	private final AtomTranslator atomTranslator;
	private final java.util.function.Predicate<Predicate> filter;
	private byte[] currentTruthValues = new byte[]{-2, -1, -1, -1, -1};
	private static Atom atomAA = new BasicAtom(Predicate.getInstance("a", 0));
	private static Atom atomBB = new BasicAtom(Predicate.getInstance("b", 0));
	private static Atom atomCC = new BasicAtom(Predicate.getInstance("c", 0));
	private static Rule ruleABC = new Rule(new DisjunctiveHead(Collections.singletonList(atomCC)), Arrays.asList(atomAA.toLiteral(), atomBB.toLiteral()));
	private static Atom rule1 = new RuleAtom(NonGroundRule.constructNonGroundRule(ruleABC), new Substitution());
	private Set<Integer> returnedNogoods = new HashSet<>();

	public DummyGrounder(AtomTranslator atomTranslator) {
		this(atomTranslator, p -> true);
	}

	public DummyGrounder(AtomTranslator atomTranslator, java.util.function.Predicate<Predicate> filter) {
		this.atomTranslator = atomTranslator;
		this.filter = filter;
		Arrays.asList(atomAA, atomBB, rule1, atomCC).forEach(atomTranslator::putIfAbsent);
	}

	@Override
	public void forgetAssignment(int[] atomIds) {
		for (int atomId : atomIds) {
			currentTruthValues[atomId] = -1;
		}
	}

	private int solverDerivedNoGoodIdCounter = 20;
	private Map<NoGood, Integer> solverDerivedNoGoods = new HashMap<>();

	@Override
	public int register(NoGood noGood) {
		if (!solverDerivedNoGoods.containsKey(noGood)) {
			solverDerivedNoGoods.put(noGood, solverDerivedNoGoodIdCounter++);
		}
		return solverDerivedNoGoods.get(noGood);
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		// Note: This grounder only deals with 0-ary predicates, i.e., every atom is a predicate and there is
		// 	 only one predicate instance representing 0 terms.

		SortedSet<Predicate> trueAtomPredicates = new TreeSet<>();
		for (int trueAtom : trueAtoms) {
			Predicate atomPredicate = atomTranslator.get(trueAtom).getPredicate();
			if (!filter.test(atomPredicate)) {
				continue;
			}
			if (atomPredicate.isInternal()) {
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
	public void updateAssignment(Iterator<Integer> it) {
		while (it.hasNext()) {
			currentTruthValues[it.next()] = 1;
		}
	}

	private void addNoGoodIfNotAlreadyReturned(Map<Integer, NoGood> integerNoGoodMap, Integer idNoGood) {
		if (!returnedNogoods.contains(idNoGood)) {
			integerNoGoodMap.put(idNoGood, NOGOODS.get(idNoGood));
			returnedNogoods.add(idNoGood);
		}
	}
}
