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
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.entriesToMap;
import static at.ac.tuwien.kr.alpha.Util.entry;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

/**
 * Represents a small ASP program with choices {@code { aa :- not bb.  bb :- not aa. }}.
 * Copyright (c) 2016, the Alpha Team.
 */
public class ChoiceGrounder implements Grounder {
	public static final Set<AnswerSet> EXPECTED = new HashSet<>(asList(
		new AnswerSetBuilder()
			.predicate("aa")
			.build(),
		new AnswerSetBuilder()
			.predicate("bb")
			.build()
	));

	private static final int ATOM_AA = 1;
	private static final int ATOM_BB = 2;
	private static final int ATOM_BR1 = 3;
	private static final int ATOM_BR2 = 4;
	private static final int ATOM_EN_BR1 = 5;
	private static final int ATOM_EN_BR2 = 6;
	private static final int ATOM_DIS_BR1 = 7;
	private static final int ATOM_DIS_BR2 = 8;
	private static final int RULE_AA = 11; // { -aa, _br1 }
	private static final int BRULE_AA = 12; // { -_br1, -bb }
	private static final int RULE_BB = 13; // { -bb, _br2 }
	private static final int BRULE_BB = 14; // { -_br2, -aa }
	private static final int CHOICE_EN_BR1 = 15; // { -_en_br1 }
	private static final int CHOICE_EN_BR2 = 16; // { -_en_br2 }
	private static final int CHOICE_DIS_BR1 = 17; // { -_dis_br1,  bb}
	private static final int CHOICE_DIS_BR2 = 18; // { -dis_br2, aa }
	private static final Map<Integer, NoGood> NOGOODS = Stream.of(
		entry(RULE_AA, NoGood.headFirst(-ATOM_AA, ATOM_BR1)),
		entry(BRULE_AA, NoGood.headFirst(-ATOM_BR1, -ATOM_BB)),
		entry(RULE_BB, NoGood.headFirst(-ATOM_BB, ATOM_BR2)),
		entry(BRULE_BB, NoGood.headFirst(-ATOM_BR2, -ATOM_AA)),
		entry(CHOICE_EN_BR1, NoGood.headFirst(-ATOM_EN_BR1)),
		entry(CHOICE_EN_BR2, NoGood.headFirst(-ATOM_EN_BR2)),
		entry(CHOICE_DIS_BR1, NoGood.headFirst(-ATOM_DIS_BR1, ATOM_BB)),
		entry(CHOICE_DIS_BR2, NoGood.headFirst(-ATOM_DIS_BR2, ATOM_AA))
	).collect(entriesToMap());
	private static final Map<Integer, Integer> CHOICE_ENABLE = Stream.of(
		entry(ATOM_BR1, ATOM_EN_BR1),
		entry(ATOM_BR2, ATOM_EN_BR2)
	).collect(entriesToMap());
	private static final Map<Integer, Integer> CHOICE_DISABLE = Stream.of(
		entry(ATOM_BR1, ATOM_DIS_BR1),
		entry(ATOM_BR2, ATOM_DIS_BR2)
	).collect(entriesToMap());
	private static Map<Integer, String> atomIdToString = Stream.of(
		entry(ATOM_AA, "aa"),
		entry(ATOM_BB, "bb"),
		entry(ATOM_BR1, "_br1"),
		entry(ATOM_BR2, "_br2"),
		entry(ATOM_EN_BR1, "_en_br1"),
		entry(ATOM_EN_BR2, "_en_br2"),
		entry(ATOM_DIS_BR1, "_dis_br1"),
		entry(ATOM_DIS_BR2, "_dis_br2")
	).collect(entriesToMap());
	private boolean returnedAllNogoods;

	private final java.util.function.Predicate<Predicate> filter;

	public ChoiceGrounder() {
		this(p -> true);
	}

	public ChoiceGrounder(java.util.function.Predicate<Predicate> filter) {
		this.filter = filter;
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		SortedSet<Predicate> trueAtomPredicates = new TreeSet<>();
		for (int trueAtom : trueAtoms) {
			Predicate atomPredicate = new Predicate(atomIdToString.get(trueAtom), 0);
			if (!filter.test(atomPredicate)) {
				continue;
			}
			if (atomPredicate.getPredicateName().startsWith("_")) {
				continue;
			}
			trueAtomPredicates.add(atomPredicate);
		}

		// Add the atom instances
		Map<Predicate, SortedSet<Atom>> predicateInstances = new HashMap<>();
		for (Predicate trueAtomPredicate : trueAtomPredicates) {
			BasicAtom basicAtom = new BasicAtom(trueAtomPredicate);
			predicateInstances.put(trueAtomPredicate, new TreeSet<>(singleton(basicAtom)));
		}

		// Note: This grounder only deals with 0-ary predicates, i.e., every atom is a predicate and there is
		// 	 only one predicate instance representing 0 terms.
		return new BasicAnswerSet(trueAtomPredicates, predicateInstances);
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(Assignment assignment) {
		if (!returnedAllNogoods) {
			returnedAllNogoods = true;
			return NOGOODS;
		} else {
			return new HashMap<>();
		}
	}
	
	private boolean isFirst = true;

	@Override
	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		if (isFirst) {
			isFirst = false;
			return new ImmutablePair<>(CHOICE_ENABLE, CHOICE_DISABLE);
		} else {
			return new ImmutablePair<>(new HashMap<>(), new HashMap<>());
		}
	}

	@Override
	public void updateAssignment(Iterator<Assignment.Entry> it) {
		// This test grounder reports all NoGoods immediately, irrespective of any assignment.
	}

	@Override
	public void forgetAssignment(int[] atomIds) {
	}

	@Override
	public String atomToString(int atomId) {
		return Integer.toString(atomId);
	}

	@Override
	public List<Integer> getUnassignedAtoms(Assignment assignment) {
		List<Integer> unassigned = new ArrayList<>();
		List<Integer> knownAtomIds = new ArrayList<>(atomIdToString.keySet());
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
		return atom == ATOM_BR1 || atom == ATOM_BR2;
	}

	@Override
	public int getMaxAtomId() {
		return 8;
	}
}
