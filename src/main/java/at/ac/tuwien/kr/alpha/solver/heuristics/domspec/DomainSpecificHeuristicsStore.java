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

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.heuristics.DomainSpecificHeuristicValues;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues.DEFAULT_LEVEL;
import static at.ac.tuwien.kr.alpha.common.heuristics.NonGroundDomainSpecificHeuristicValues.DEFAULT_WEIGHT;

/**
 * Stores a mapping between rule atom IDs and their corresponding domain-specific heuristic values
 */
public interface DomainSpecificHeuristicsStore {
	
	public class Entry {
		private final int level;
		private final int weight;
		private final int choicePoint;

		Entry(int level, int weight, int choicePoint) {
			this.level = level;
			this.weight = weight;
			this.choicePoint = choicePoint;
		}

		public int getLevel() {
			return level;
		}

		public int getWeight() {
			return weight;
		}

		public int getChoicePoint() {
			return choicePoint;
		}
		
		public boolean isDefaultPriority() {
			return level == DEFAULT_LEVEL && weight == DEFAULT_WEIGHT;
		}
		
		@Override
		public String toString() {
			return String.format("%d [%d@%d]", choicePoint, weight, level);
		}
	}

	void addInfo(DomainSpecificHeuristicValues domainSpecificHeuristicsInfo);

	default void addInfo(Map<Integer, DomainSpecificHeuristicValues> domainSpecificHeuristicsInfo) {
		for (DomainSpecificHeuristicValues values : domainSpecificHeuristicsInfo.values()) {
			addInfo(values);
		}
	}

	/**
	 * Streams sets of rule atoms, each of which contains only rule atoms of the same priority. The stream returns rule atoms in decreasing order of priority,
	 * e.g. the first set contains all rule atoms of the highest weight and the highest level, the second contains all of the highest weight and the
	 * second-to-highest level, etc.
	 */
	Stream<Set<Entry>> streamEntriesOrderedByDecreasingPriority();
	
	/**
	 * Returns the set of literals that have to be true for the non-default heuristic values to apply to the given rule.
	 */
	Collection<Integer> getConditionLiterals(int ruleAtomId);
	
	/**
	 * Checks if the condition for the given rule is satisfied by the given assignment
	 * @param ruleAtomId the ID of a body-representing atom
	 * @param assignment
	 * @return {@code true} iff for none of the literals in {@link #getConditionLiterals(int)} it holds that {@link Assignment#isViolated(int)} for the given {@code assignment}
	 */
	default boolean isConditionSatisfied(int ruleAtomId, Assignment assignment) {
		return getConditionLiterals(ruleAtomId).stream().noneMatch(assignment::isUnsatisfied);
	}
	
	Set<Entry> getAllEntries();

	/**
	 * Returns the set of choice points which are either defined to have priority 1@1 or whose condition is not satisfied under the given assignment
	 */
	default Set<Integer> getChoicePointsWithDefaultPriority(Assignment assignment) {
		return getAllEntries().stream().filter(e -> e.isDefaultPriority() || !isConditionSatisfied(e.choicePoint, assignment)).map(Entry::getChoicePoint).collect(Collectors.toSet());
	}

}
