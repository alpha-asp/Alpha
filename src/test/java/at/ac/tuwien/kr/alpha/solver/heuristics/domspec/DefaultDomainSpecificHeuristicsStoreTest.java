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

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues;
import at.ac.tuwien.kr.alpha.solver.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Test {@link DefaultDomainSpecificHeuristicsStore}
 */
public class DefaultDomainSpecificHeuristicsStoreTest {
	private static final int INITIAL_GENERATED_ID = 100;

	private final AtomStore atomStore;
	private final WritableAssignment assignment;
	private ChoiceManager choiceManager;
	private DefaultDomainSpecificHeuristicsStore store = new DefaultDomainSpecificHeuristicsStore(null);
	private AtomicInteger idGenerator = new AtomicInteger(INITIAL_GENERATED_ID);
	
	public DefaultDomainSpecificHeuristicsStoreTest() {
		atomStore = new AtomStoreImpl();
		assignment = new TrailAssignment(atomStore);
	}

	@Before
	public void setUp() throws IOException {
		this.choiceManager = new PseudoChoiceManager(assignment, new NaiveNoGoodStore(assignment));
		this.store = new DefaultDomainSpecificHeuristicsStore(choiceManager);
	}

	
	@Test
	public void testInsert_1_atom() {
		int id = idGenerator.getAndIncrement();
		store.addInfo(id, info(1, 1, 1));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(1, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(id), nextSetOfChoicePoints(iterator));
	}

	@Test
	public void testInsert_2_atoms_sameWeight_sameLevel() {
		int id1 = idGenerator.getAndIncrement();
		int id2 = idGenerator.getAndIncrement();
		store.addInfo(id1, info(1, 2, 3));
		store.addInfo(id2, info(2, 2, 3));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(1, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(id1, id2), nextSetOfChoicePoints(iterator));
	}

	@Test
	public void testInsert_3_atoms_sameWeight_differentLevel() {
		int id1 = idGenerator.getAndIncrement();
		int id2 = idGenerator.getAndIncrement();
		int id3 = idGenerator.getAndIncrement();
		store.addInfo(id1, info(1, 2, 3));
		store.addInfo(id2, info(2, 2, 1));
		store.addInfo(id3, info(3, 2, 2));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(3, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(id1), nextSetOfChoicePoints(iterator));
		assertEquals(set(id3), nextSetOfChoicePoints(iterator));
		assertEquals(set(id2), nextSetOfChoicePoints(iterator));
	}

	@Test
	public void testInsert_3_atoms_differentWeight_sameLevel() {
		int id1 = idGenerator.getAndIncrement();
		int id2 = idGenerator.getAndIncrement();
		int id3 = idGenerator.getAndIncrement();
		store.addInfo(id1, info(1, 4, 1));
		store.addInfo(id2, info(2, 2, 1));
		store.addInfo(id3, info(3, 3, 1));
		Collection<Set<Integer>> orderedList = store.getHeuristicsOrderedByDecreasingPriority();
		assertEquals(3, orderedList.size());
		Iterator<Set<Integer>> iterator = orderedList.iterator();
		assertEquals(set(id1), nextSetOfChoicePoints(iterator));
		assertEquals(set(id3), nextSetOfChoicePoints(iterator));
		assertEquals(set(id2), nextSetOfChoicePoints(iterator));
	}

	private HeuristicDirectiveValues info(int atom, int weight, int level) {
		return new HeuristicDirectiveValues(atom, weight, level, true);
	}

	@SafeVarargs
	private static final <T> Set<T> set(T... elements) {
		return Arrays.stream(elements).collect(Collectors.toSet());
	}

	private Set<Integer> nextSetOfChoicePoints(Iterator<Set<Integer>> iterator) {
		return iterator.next().stream().collect(Collectors.toSet());
	}

	private class PseudoChoiceManager extends ChoiceManager {

		public PseudoChoiceManager(WritableAssignment assignment, NoGoodStore store) {
			super(assignment, store);
		}
		
		@Override
		public Set<Integer> getAllActiveHeuristicAtoms() {
			Set<Integer> generatedIDs = new HashSet<>();
			int maxID = idGenerator.get();
			for (int i = INITIAL_GENERATED_ID; i <= maxID; i++) {
				generatedIDs.add(i);
			}
			return generatedIDs;
		}
	}

}
