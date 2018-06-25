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
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Test {@link DefaultDomainSpecificHeuristicsStore}
 */
public class DefaultDomainSpecificHeuristicsStoreTest {

	private final DefaultDomainSpecificHeuristicsStore store = new DefaultDomainSpecificHeuristicsStore();

	@Test
	public void testInsert_1_atom() {
		store.addInfo(100, info(1, 1, 1));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(1, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(100), nextSetOfChoicePoints(iterator));
	}

	@Test
	public void testInsert_2_atoms_sameWeight_sameLevel() {
		store.addInfo(100, info(1, 2, 3));
		store.addInfo(101, info(2, 2, 3));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(1, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(100, 101), nextSetOfChoicePoints(iterator));
	}

	@Test
	public void testInsert_3_atoms_sameWeight_differentLevel() {
		store.addInfo(100, info(1, 2, 3));
		store.addInfo(101, info(2, 2, 1));
		store.addInfo(102, info(3, 2, 2));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(3, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(100), nextSetOfChoicePoints(iterator));
		assertEquals(set(102), nextSetOfChoicePoints(iterator));
		assertEquals(set(101), nextSetOfChoicePoints(iterator));
	}

	@Test
	public void testInsert_3_atoms_differentWeight_sameLevel() {
		store.addInfo(100, info(1, 4, 1));
		store.addInfo(101, info(2, 2, 1));
		store.addInfo(102, info(3, 3, 1));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(3, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(100), nextSetOfChoicePoints(iterator));
		assertEquals(set(102), nextSetOfChoicePoints(iterator));
		assertEquals(set(101), nextSetOfChoicePoints(iterator));
	}

	private HeuristicDirectiveValues info(int atom, int weight, int level) {
		return new HeuristicDirectiveValues(atom, weight, level, true);
	}

	@SafeVarargs
	private final <T> Set<T> set(T... elements) {
		return Arrays.stream(elements).collect(Collectors.toSet());
	}

	private Set<Integer> nextSetOfChoicePoints(Iterator<Set<Integer>> iterator) {
		return iterator.next().stream().collect(Collectors.toSet());
	}

}
