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
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores a mapping between heuristic IDs and their corresponding domain-specific heuristic values.
 * 
 * The mapping is stored in the form of a hierarchical map.
 */
public class DefaultDomainSpecificHeuristicsStore implements DomainSpecificHeuristicsStore {
	
	/**
	 * A switch that indicates whether priorities shall be computed on demand just for heuristics currently active.
	 * If {@code true}, the internal map of levels and weights to heuristics will be re-computed for currently active heuristics every time {@link #streamHeuristicsOrderedByDecreasingPriority()} is called;
	 * if {@code false}, it will be extended each time {@link #addInfo(int, HeuristicDirectiveValues)} is called.
	 */
	private static final boolean COMPUTE_PRIORITIES_ON_DEMAND = true;

	/**
	 * A mapping from levels to a mapping from weights to sets of heuristic IDs
	 * (level -> { weight -> { heuristics } }).
	 * Maps are {@link TreeMap}s sorted in reverse order, which determines the order in which elements are retrieved by
	 * {@link #streamEntriesOrderedByDecreasingPriority()}.
	 */
	private final SortedMap<Integer, SortedMap<Integer, Set<Integer>>> mapLevelWeightHeuristics = new TreeMap<>(Comparator.reverseOrder());

	private final Map<Integer, HeuristicDirectiveValues> mapHeuristicToHeuristicValue = new HashMap<>();

	private ChoiceManager choiceManager;
	
	public DefaultDomainSpecificHeuristicsStore(ChoiceManager choiceManager) {
		super();
		this.choiceManager = choiceManager;
	}

	@Override
	public void addInfo(int heuristicId, HeuristicDirectiveValues values) {
		if (!COMPUTE_PRIORITIES_ON_DEMAND) {
			storePriorityInfo(heuristicId, values);
		}
		
		mapHeuristicToHeuristicValue.put(heuristicId, values);
	}

	void storePriorityInfo(int heuristicId, HeuristicDirectiveValues values) {
		int level = values.getLevel();
		int weight = values.getWeight();

		Set<Integer> valuesForWeightLevel = getStoreForWeightLevel(weight, getStoreForLevel(level));
		valuesForWeightLevel.add(heuristicId);
	}

	SortedMap<Integer, Set<Integer>> getStoreForLevel(int level) {
		SortedMap<Integer, Set<Integer>> mapWeightValues = mapLevelWeightHeuristics.get(level);
		if (mapWeightValues == null) {
			mapWeightValues = new TreeMap<>(Comparator.reverseOrder());
			mapLevelWeightHeuristics.put(level, mapWeightValues);
		}
		return mapWeightValues;
	}

	Set<Integer> getStoreForWeightLevel(int weight, SortedMap<Integer, Set<Integer>> mapWeightValues) {
		Set<Integer> valuesForWeightLevel = mapWeightValues.get(weight);
		if (valuesForWeightLevel == null) {
			valuesForWeightLevel = new HashSet<>();
			mapWeightValues.put(weight, valuesForWeightLevel);
		}
		return valuesForWeightLevel;
	}

	@Override
	public Collection<Set<Integer>> getHeuristicsOrderedByDecreasingPriority() {
		Stream<Set<Integer>> flatMap = streamHeuristicsOrderedByDecreasingPriority();
		// do not return flatMap directly because of Java bug
		// cf. https://stackoverflow.com/questions/29229373/why-filter-after-flatmap-is-not-completely-lazy-in-java-streams
		return flatMap.collect(Collectors.toList());
	}

	/**
	 * Note: we have already tried to use this method instead of getHeuristicsOrderedByDecreasingPriority
	 * in at.ac.tuwien.kr.alpha.solver.heuristics.DomainSpecific.chooseLiteral(Set<Integer>),
	 * but it did not yield performance benefits
	 */
	@Override
	public Stream<Set<Integer>> streamHeuristicsOrderedByDecreasingPriority() {
		if (COMPUTE_PRIORITIES_ON_DEMAND) {
			updatePriorityInfos();
		}
		return mapLevelWeightHeuristics.values().stream().flatMap(m -> m.values().stream());
	}
	
	private void updatePriorityInfos() {
		mapLevelWeightHeuristics.clear();
		for (Integer activeHeuristic : choiceManager.getAllActiveHeuristicAtoms()) {
			HeuristicDirectiveValues values = mapHeuristicToHeuristicValue.get(activeHeuristic);
			if (values != null) {
				storePriorityInfo(activeHeuristic, values);
			}
		}
	}
	
	@Override
	public HeuristicDirectiveValues getValues(int heuristicId) {
		return mapHeuristicToHeuristicValue.get(heuristicId);
	}

}
