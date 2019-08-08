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
package at.ac.tuwien.kr.alpha.solver.heuristics.domspec;

import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues.PriorityComparator;
import at.ac.tuwien.kr.alpha.solver.ChoiceInfluenceManager;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;

import java.util.*;
import java.util.Map.Entry;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Stores a mapping between heuristic IDs and their corresponding domain-specific heuristic values.
 * 
 * The mapping is stored in the form of a hierarchical map.
 */
public class DefaultDomainSpecificHeuristicsStore implements DomainSpecificHeuristicsStore {

	/**
	 * A mapping from levels to a mapping from weights to sets of heuristic IDs
	 * (level -> { weight -> { heuristics } }).
	 * Maps are {@link TreeMap}s sorted in reverse order, which determines the order in which elements are retrieved by
	 * {@link #streamEntriesOrderedByDecreasingPriority()}.
	 */
	private final SortedMap<Integer, SortedMap<Integer, Set<Integer>>> mapLevelWeightHeuristics = new TreeMap<>(Comparator.reverseOrder());

	private final Map<Integer, HeuristicDirectiveValues> mapHeuristicToHeuristicValue = new HashMap<>();

	private PriorityQueue<Integer> prioritisedHeuristics = new PriorityQueue<>(new HeuristicPriorityComparator().reversed());

	private ChoiceManager choiceManager;
	private boolean checksEnabled;

	@Override
	public void addInfo(int heuristicId, HeuristicDirectiveValues values) {
		if (checksEnabled) {
			storePriorityInfo(heuristicId, values);
		}

		mapHeuristicToHeuristicValue.put(heuristicId, values);
	}

	private void storePriorityInfo(int heuristicId, HeuristicDirectiveValues values) {
		int level = values.getLevel();
		int weight = values.getWeight();

		Set<Integer> valuesForWeightLevel = getStoreForWeightLevel(weight, getStoreForLevel(level));
		valuesForWeightLevel.add(heuristicId);
	}

	private SortedMap<Integer, Set<Integer>> getStoreForLevel(int level) {
		SortedMap<Integer, Set<Integer>> mapWeightValues = mapLevelWeightHeuristics.get(level);
		if (mapWeightValues == null) {
			mapWeightValues = new TreeMap<>(Comparator.reverseOrder());
			mapLevelWeightHeuristics.put(level, mapWeightValues);
		}
		return mapWeightValues;
	}

	private Set<Integer> getStoreForWeightLevel(int weight, SortedMap<Integer, Set<Integer>> mapWeightValues) {
		Set<Integer> valuesForWeightLevel = mapWeightValues.get(weight);
		if (valuesForWeightLevel == null) {
			valuesForWeightLevel = new HashSet<>();
			mapWeightValues.put(weight, valuesForWeightLevel);
		}
		return valuesForWeightLevel;
	}

	@Override
	public HeuristicDirectiveValues poll() {
		Integer heuristicId = prioritisedHeuristics.poll();
		return heuristicId == null ? null : mapHeuristicToHeuristicValue.get(heuristicId);
	}

	@Override
	public HeuristicDirectiveValues peek() {
		Integer heuristicId = prioritisedHeuristics.peek();
		return heuristicId == null ? null : mapHeuristicToHeuristicValue.get(heuristicId);
	}

	private void checkPrioritisedHeuristics() {
		for (Entry<Integer, SortedMap<Integer, Set<Integer>>> levelWeightEntry : mapLevelWeightHeuristics.entrySet()) {
			int level = levelWeightEntry.getKey();
			for (Entry<Integer, Set<Integer>> weightHeuristicsEntry : levelWeightEntry.getValue().entrySet()) {
				int weight = weightHeuristicsEntry.getKey();
				for (int heuristicId : weightHeuristicsEntry.getValue()) {
					HeuristicDirectiveValues values = mapHeuristicToHeuristicValue.get(heuristicId);
					if (values.getLevel() != level) {
						throw oops("Unexpected level in " + values + " (expected " + level + ")");
					}
					if (values.getWeight() != weight) {
						throw oops("Unexpected weight in " + values + " (expected " + weight + ")");
					}
					if (choiceManager.isActiveHeuristicAtom(heuristicId)) {
						if (!prioritisedHeuristics.contains(heuristicId)) {
							throw oops("Heap of prioritised heuristics does not contain " + values);
						}
					} else {
						if (prioritisedHeuristics.contains(heuristicId)) {
							throw oops("Heap of prioritised heuristics contains " + values);
						}
					}
				}
			}
		}
	}

	@Override
	public HeuristicDirectiveValues getValues(int heuristicId) {
		return mapHeuristicToHeuristicValue.get(heuristicId);
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = false;
		// TODO: internal checks do not work currently because, to increase efficiency,
		// values whose head will be set are not offered back by at.ac.tuwien.kr.alpha.solver.heuristics.DomainSpecific.chooseLiteral(Set<Integer>)
	}

	@Override
	public void setChoiceManager(ChoiceManager choiceManager) {
		this.choiceManager = choiceManager;
		if (choiceManager != null) {
			this.choiceManager.setHeuristicActivityListener(new HeuristicActivityListener());
		}
	}

	private class HeuristicActivityListener implements ChoiceInfluenceManager.ActivityListener {

		@Override
		public void callbackOnChanged(int atom, boolean active) {
			if (active) {
				// TODO: check if it is possible and useful to test for the following condition:
				// if (assignment.isUnassignedOrMBT(values.getHeadAtomId())) {
				prioritisedHeuristics.add(atom);
				// }
			} else {
				prioritisedHeuristics.remove(atom);
			}
			if (checksEnabled) {
				checkPrioritisedHeuristics();
			}
		}
	}
	
	private class HeuristicPriorityComparator implements Comparator<Integer> {
		
		private final PriorityComparator priorityComparator = new PriorityComparator();

		@Override
		public int compare(Integer heu1, Integer heu2) {
			return priorityComparator.compare(mapHeuristicToHeuristicValue.get(heu1), mapHeuristicToHeuristicValue.get(heu2));
		}

	}

}
