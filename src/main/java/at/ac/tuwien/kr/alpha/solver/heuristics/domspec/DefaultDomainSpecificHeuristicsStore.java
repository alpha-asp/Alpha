/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores a mapping between rule atom IDs and their corresponding domain-specific heuristic values.
 * 
 * The mapping is stored in the form of a hierarchical map.
 * 
 * TODO: this class has already partially been refactored from heuristic atoms/annotations to heuristic directives, has to be cleaned up
 * TODO: is it better to constantly maintain a map of all heuristics (as is currently the case) or to compute just the map of currently active heuristics when a choice has to be made? 
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

	@Override
	public void addInfo(int heuristicId, HeuristicDirectiveValues values) {
		int level = values.getLevel();
		int weight = values.getWeight();

		SortedMap<Integer, Set<Integer>> mapWeightValues = mapLevelWeightHeuristics.get(level);
		if (mapWeightValues == null) {
			mapWeightValues = new TreeMap<>(Comparator.reverseOrder());
			mapLevelWeightHeuristics.put(level, mapWeightValues);
		}
		
		Set<Integer> valuesForWeightLevel = mapWeightValues.get(weight);
		if (valuesForWeightLevel == null) {
			valuesForWeightLevel = new HashSet<>();
			mapWeightValues.put(weight, valuesForWeightLevel);
		}
		valuesForWeightLevel.add(heuristicId);
		
		mapHeuristicToHeuristicValue.put(heuristicId, values);
	}

	@Override
	public Collection<Set<Integer>> getHeuristicsOrderedByDecreasingPriority() {
		Stream<Set<Integer>> flatMap = mapLevelWeightHeuristics.values().stream().flatMap(m -> m.values().stream());
		// do not return flatMap directly because of Java bug
		// cf. https://stackoverflow.com/questions/29229373/why-filter-after-flatmap-is-not-completely-lazy-in-java-streams
		return flatMap.collect(Collectors.toList());
	}
	
	@Override
	public Set<Integer> getAllEntries() {
		Set<Integer> entries = new HashSet<>();
		for (SortedMap<Integer, Set<Integer>> mapWeightChoicePoint : mapLevelWeightHeuristics.values()) {
			for (Set<Integer> entriesForCurrentPriority : mapWeightChoicePoint.values()) {
				entries.addAll(entriesForCurrentPriority);
			}
		}
		return entries;
	}
	
	@Override
	public HeuristicDirectiveValues getValues(int heuristicId) {
		return mapHeuristicToHeuristicValue.get(heuristicId);
	}

}
