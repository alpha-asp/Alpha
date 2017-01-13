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
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.solver.Choices;

import java.util.*;

/**
 * A semi-naive grounder.
 * Copyright (c) 2016, the Alpha Team.
 */
public class NaiveGrounder extends BridgedGrounder {
	private final IntIdGenerator nogoodIdGenerator = new IntIdGenerator();
	private final IntIdGenerator choiceAtomsGenerator = new IntIdGenerator();

	private final Map<NoGood, Integer> nogoodIdentifiers = new HashMap<>();
	private final AtomStore atomStore = new AtomStore();
	private Choices choices = new Choices();

	public NaiveGrounder(Bridge... bridges) {
		this(p -> true, bridges);
	}

	public NaiveGrounder(java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		super(filter, bridges);
	}

	@Override
	public AnswerSet assignmentToAnswerSet(Iterable<Integer> trueAtoms) {
		Map<Predicate, SortedSet<Atom>> predicateInstances = new HashMap<>();
		SortedSet<Predicate> knownPredicates = new TreeSet<>();

		if (!trueAtoms.iterator().hasNext()) {
			return BasicAnswerSet.EMPTY;
		}

		// Iterate over all true atomIds, computeNextAnswerSet instances from atomStore and add them if not filtered.
		for (int trueAtom : trueAtoms) {
			Atom basicAtom = atomStore.get(trueAtom);
			Predicate predicate = basicAtom.getPredicate();

			// Skip internal atoms and filtered predicates.
			if (basicAtom.isInternal() || !filter.test(predicate)) {
				continue;
			}

			knownPredicates.add(basicAtom.getPredicate());
			predicateInstances.putIfAbsent(basicAtom.getPredicate(), new TreeSet<>());
			SortedSet<Atom> instances = predicateInstances.get(basicAtom.getPredicate());
			instances.add(basicAtom);
		}

		if (knownPredicates.isEmpty()) {
			return BasicAnswerSet.EMPTY;
		}

		return new BasicAnswerSet(knownPredicates, predicateInstances);
	}

	@Override
	public Map<Integer, NoGood> getNoGoods(ReadableAssignment assignment) {
		Map<Integer, NoGood> newNoGoods = new HashMap<>();

		// Import additional noGoods from external sources
		for (NoGood noGood : collectExternalNogoods(assignment, atomStore, choices, choiceAtomsGenerator)) {
			registerIfNeeded(noGood, newNoGoods);
		}

		return newNoGoods;
	}

	@Override
	public Choices getChoices() {
		Choices currentChoices = choices;
		choices = new Choices();
		return currentChoices;
	}

	@Override
	public void updateAssignment(Iterator<? extends ReadableAssignment.Entry> it) {
		while (it.hasNext()) {
			ReadableAssignment.Entry assignment = it.next();
			for (Bridge bridge : bridges) {
				bridge.updateAssignment(atomStore.get(assignment.getAtom()), assignment.getTruth());
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
	public List<Integer> getUnassignedAtoms(ReadableAssignment assignment) {
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

	public void printCurrentlyKnownNoGoods() {
		System.out.println("Printing known NoGoods:");
		for (Map.Entry<NoGood, Integer> noGoodEntry : nogoodIdentifiers.entrySet()) {
			System.out.println(noGoodEntry.getKey().toStringReadable(this));
		}
	}

	private boolean registerIfNeeded(NoGood noGood, Map<Integer, NoGood> target) {
		if (nogoodIdentifiers.containsKey(noGood)) {
			return false;
		}
		int noGoodId = nogoodIdGenerator.getNextId();
		nogoodIdentifiers.put(noGood, noGoodId);
		target.put(noGoodId, noGood);
		return true;
	}
}
