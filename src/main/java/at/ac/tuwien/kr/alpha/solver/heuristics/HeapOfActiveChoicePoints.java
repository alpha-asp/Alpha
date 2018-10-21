/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.ChoiceInfluenceManager;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.HashSet;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

/**
 * Extends {@code HeapOfActiveAtoms} by a mechanism that,
 * when the activity of an atom is increased, the increase counts towards dependent choice point
 * if the given atom is not a choice point itself.
 * Also, only choice points are maintained in the heap and only choice points are returned by
 * {@link #getMostActiveAtom()}.
 *
 */
public class HeapOfActiveChoicePoints extends HeapOfActiveAtoms {
	
	private ChoiceManager choiceManager;

	/**
	 * Maps atoms to choice points representing bodies of rules in which the former atoms occur
	 * (in the head or the body).
	 */
	protected final MultiValuedMap<Integer, Integer> atomsToChoicePoints = new HashSetValuedHashMap<>();
	
	public HeapOfActiveChoicePoints(int decayAge, double decayFactor, ChoiceManager choiceManager) {
		super(decayAge, decayFactor);
		this.choiceManager = choiceManager;
		this.choiceManager.addChoicePointActivityListener(new HeuristicActivityListener());
	}
	
	@Override
	public void incrementActivity(int atom) {
		if (choiceManager.isAtomChoice(atom)) {
			super.incrementActivity(atom);
		} else {
			for (Integer dependentBody : atomsToChoicePoints.get(atom)) {
				super.incrementActivity(dependentBody);
			}
		}
	}
	
	/**
	 * Records the dependency relationships between atoms occurring in the given nogood.
	 * TODO: resolve duplication between this method and {@link GeneralizedDependencyDrivenHeuristic#recordAtomRelationships(NoGood)}
	 * @param noGood
	 */
	private void recordAtomRelationships(NoGood noGood) {
		final int none = -1;
		int body = none;
		Set<Integer> others = new HashSet<>();
		
		for (int literal : noGood) {
			int atom = atomOf(literal);
			if (body == none && choiceManager.isAtomChoice(atom)) {
				body = atom;
			} else {
				others.add(atom);
			}
		}
		
		if (body == none) {
			return;
		}
		
		for (Integer atom : others) {
			atomsToChoicePoints.put(atom, body);
		}
	}

	/**
	 * TODO: Computes and stores initial activity values for the atoms occurring in the given nogood.
	 * TODO: replace by MOMs, for example
	 * @param newNoGood
	 */
	public void initActity(NoGood newNoGood) {
		recordAtomRelationships(newNoGood);
		for (Integer literal : newNoGood) {
			incrementActivity(atomOf(literal));
		}
	}
	
	@Override
	public String toString() {
		return heap.toString();
	}

	private class HeuristicActivityListener implements ChoiceInfluenceManager.ActivityListener {

		@Override
		public void callbackOnChanged(int atom, boolean active) {
			if (active && choiceManager.isActiveChoiceAtom(atom)) {
				Double activity = activityScores.get(atom);
				if (activity != null) {
					/* if activity is null, probably the atom is still being buffered
					   by DependencyDrivenVSIDSHeuristic and will get an initial activity
					   when the buffer is ingested */
					heap.add(atom);
				}
			}
		}
	}

}