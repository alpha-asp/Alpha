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

import at.ac.tuwien.kr.alpha.common.heuristics.DomainSpecificHeuristicValues;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores a mapping between rule atom IDs and their corresponding domain-specific heuristic values.
 * 
 * The mapping is stored in the form of a hierarchical map.
 */
public class DefaultDomainSpecificHeuristicsStore implements DomainSpecificHeuristicsStore {

	/**
	 * A mapping from weights to a mapping from levels to sets of rule atom IDs
	 * (weight -> { level -> { rules } }).
	 * Maps are {@link TreeMap}s sorted in reverse order, which determines the order in which elements are retrieved by
	 * {@link #streamRuleAtomsOrderedByDecreasingPriority()}.
	 */
	private final SortedMap<Integer, SortedMap<Integer, Set<Integer>>> mapWeightLevelChoicePoint = new TreeMap<>(Comparator.reverseOrder());

	@Override
	public void addInfo(DomainSpecificHeuristicValues info) {
		int weight = info.getWeight();
		int level = info.getLevel();

		if (!mapWeightLevelChoicePoint.containsKey(weight)) {
			mapWeightLevelChoicePoint.put(weight, new TreeMap<>(Comparator.reverseOrder()));
		}

		Map<Integer, Set<Integer>> mapLevelChoicePoint = mapWeightLevelChoicePoint.get(weight);
		if (!mapLevelChoicePoint.containsKey(level)) {
			mapLevelChoicePoint.put(level, new HashSet<>());
		}

		mapLevelChoicePoint.get(level).add(info.getRuleAtomId());
	}

	@Override
	public Stream<Set<Integer>> streamRuleAtomsOrderedByDecreasingPriority() {
		Stream<Set<Integer>> flatMap = mapWeightLevelChoicePoint.values().stream().flatMap(m -> m.values().stream());
		// do not return flatMap directly because of Java bug
		// cf. https://stackoverflow.com/questions/29229373/why-filter-after-flatmap-is-not-completely-lazy-in-java-streams
		return flatMap.collect(Collectors.toList()).stream();
	}

}
