/**
 * Copyright (c) 2016-2019, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.solver.learning;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.AtomStoreTest;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2016-2019, the Alpha Team.
 */
public class FirstUIPPriorityQueueTest {
	private final AtomStore atomStore;
	private final WritableAssignment assignment;

	public FirstUIPPriorityQueueTest() {
		atomStore = new AtomStoreImpl(true);
		assignment = new TrailAssignment(atomStore);
	}

	@Before
	public void setUp() {
		assignment.clear();
		AtomStoreTest.fillAtomStore(atomStore, 4);
		assignment.growForMaxAtomId();
	}

	@Test
	public void nonlinearEntries() {
		assignment.choose(1, ThriceTruth.MBT);
		assignment.assign(2, ThriceTruth.TRUE);
		assignment.assign(3, ThriceTruth.FALSE);
		assignment.assign(4, ThriceTruth.MBT);

		Assignment.Entry entry1 = assignment.get(1);
		Assignment.Entry entry2 = assignment.get(2);
		Assignment.Entry entry3 = assignment.get(3);
		Assignment.Entry entry4 = assignment.get(4);
		assertTrue(entry1.getDecisionLevel() == entry2.getDecisionLevel() && entry2.getDecisionLevel() == entry3.getDecisionLevel() && entry3.getDecisionLevel() == entry4.getDecisionLevel());
		assertTrue(entry1.getPropagationLevel() < entry2.getPropagationLevel());
		assertTrue(entry2.getPropagationLevel() < entry3.getPropagationLevel());
		assertTrue(entry3.getPropagationLevel() < entry4.getPropagationLevel());

		FirstUIPPriorityQueue firstUIPPriorityQueue = new FirstUIPPriorityQueue(entry1.getDecisionLevel());
		firstUIPPriorityQueue.add(entry2);
		firstUIPPriorityQueue.add(entry4);
		firstUIPPriorityQueue.add(entry3);

		assertEquals(3, firstUIPPriorityQueue.size());
		assertEquals(entry4, firstUIPPriorityQueue.poll());
		assertEquals(entry3, firstUIPPriorityQueue.poll());
		assertEquals(entry2, firstUIPPriorityQueue.poll());
	}

	@Test
	public void ignoreDuplicates() {
		assignment.choose(1, ThriceTruth.MBT);
		assignment.assign(2, ThriceTruth.TRUE);
		assignment.assign(3, ThriceTruth.FALSE);

		Assignment.Entry entry1 = assignment.get(1);
		Assignment.Entry entry2 = assignment.get(2);
		Assignment.Entry entry3 = assignment.get(3);

		FirstUIPPriorityQueue firstUIPPriorityQueue = new FirstUIPPriorityQueue(entry1.getDecisionLevel());
		firstUIPPriorityQueue.add(entry1);
		firstUIPPriorityQueue.add(entry2);
		firstUIPPriorityQueue.add(entry2);
		firstUIPPriorityQueue.add(entry3);
		firstUIPPriorityQueue.add(entry2);
		firstUIPPriorityQueue.add(entry3);
		firstUIPPriorityQueue.add(entry2);

		assertEquals(3, firstUIPPriorityQueue.size());
		assertEquals(entry3, firstUIPPriorityQueue.poll());
		assertEquals(entry2, firstUIPPriorityQueue.poll());
		assertEquals(entry1, firstUIPPriorityQueue.poll());
		assertEquals(null, firstUIPPriorityQueue.poll());
	}

}