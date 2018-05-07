/**
 * Copyright (c) 2017-2018, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.atoms.HeuristicAtom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom.off;
import static at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom.on;
import static java.util.Collections.emptyList;

public class ChoiceRecorder {
	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();

	private final AtomStore atomStore;
	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newHeuristicAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());

	public ChoiceRecorder(AtomStore atomStore) {
		this.atomStore = atomStore;
	}

	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getAndResetChoices() {
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> currentChoiceAtoms = newChoiceAtoms;
		newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
		return currentChoiceAtoms;
	}

	public Pair<Map<Integer, Integer>, Map<Integer, Integer>> getAndResetHeuristics() {
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> currentHeuristicAtoms = newHeuristicAtoms;
		newHeuristicAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
		return currentHeuristicAtoms;
	}

	public List<NoGood> generateChoiceNoGoods(final List<Integer> pos, final List<Integer> neg, final int bodyAtom) {
		// Obtain an ID for this new choice.
		final int choiceId = ID_GENERATOR.getNextId();
		// Create ChoiceOn and ChoiceOff atoms.
		final int choiceOnAtom = atomStore.add(on(choiceId));
		newChoiceAtoms.getLeft().put(bodyAtom, choiceOnAtom);
		final int choiceOffAtom = atomStore.add(off(choiceId));
		newChoiceAtoms.getRight().put(bodyAtom, choiceOffAtom);

		final List<NoGood> noGoods = generateNeg(choiceOffAtom, neg);
		noGoods.add(generatePos(choiceOnAtom, pos));

		return noGoods;
	}

	public Collection<NoGood> generateHeuristicNoGoods(final List<Integer> pos, final List<Integer> neg, HeuristicAtom groundHeuristicAtom, final int headId) {
		// Create HeuristicOn and HeuristicOff atoms.
		final int heuristicOnAtom = atomStore.add(HeuristicAtom.on(groundHeuristicAtom, headId));
		newHeuristicAtoms.getLeft().put(headId, heuristicOnAtom);
		final int heuristicOffAtom = atomStore.add(HeuristicAtom.off(groundHeuristicAtom, headId));
		newHeuristicAtoms.getRight().put(headId, heuristicOffAtom);

		final List<NoGood> noGoods = generateNeg(heuristicOffAtom, neg);
		noGoods.add(generatePos(heuristicOnAtom, pos));

		return noGoods;
	}

	private NoGood generatePos(final int atomOn, List<Integer> pos) {
		// Choice/Heuristic is on if all positive atoms are assigned true.
		return NoGood.fromBody(pos, emptyList(), atomOn);
	}

	private List<NoGood> generateNeg(final int atomOff, List<Integer> neg)  {
		final List<NoGood> noGoods = new ArrayList<>(neg.size() + 1);
		for (Integer negAtom : neg) {
			// Choice/Heuristic is off if any of the negative atoms is assigned true,
			// hence we add one nogood for each such atom.
			noGoods.add(NoGood.headFirst(-atomOff, negAtom));
		}
		return noGoods;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[enablers: ");
		for (Map.Entry<Integer, Integer> enablers : newChoiceAtoms.getLeft().entrySet()) {
			sb.append(enablers.getKey()).append("/").append(enablers.getValue()).append(", ");
		}
		sb.append(" disablers: ");
		for (Map.Entry<Integer, Integer> disablers : newChoiceAtoms.getRight().entrySet()) {
			sb.append(disablers.getKey()).append("/").append(disablers.getValue());
		}
		return sb.append("]").toString();
	}
}
