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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.NoGoodStoreAlphaRoaming;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link HeapOfActiveAtoms}, including initial heuristic scores computed by {@link MOMs}.
 *
 */
public class HeapOfActiveAtomsTest {

	private static final double DOUBLE_COMPARISON_EPSILON = 0.001;

	private AtomStore atomStore;
	private WritableAssignment assignment;
	private HeapOfActiveAtoms heapOfActiveAtoms;
	private NoGoodStoreAlphaRoaming noGoodStore;

	@Before
	public void setUp() {
		atomStore = new AtomStoreImpl();
		assignment = new TrailAssignment(atomStore);
		noGoodStore = new NoGoodStoreAlphaRoaming(assignment);
		ChoiceManager choiceManager = new PseudoChoiceManager(assignment, noGoodStore);
		this.heapOfActiveAtoms = new HeapOfActiveAtoms(1, 1, choiceManager);
	}

	@Test
	public void testAllAtomsEqualScore() {
		int lit1 = Literals.atomToLiteral(1);
		int lit2 = Literals.atomToLiteral(2);
		int lit3 = Literals.atomToLiteral(3);
		HeuristicTestUtils.addNoGoods(atomStore, assignment, noGoodStore, heapOfActiveAtoms, new NoGood(lit1, lit2), new NoGood(lit2, lit3),
				new NoGood(lit1, lit3));
		double activity1 = heapOfActiveAtoms.getActivity(lit1);
		double activity2 = heapOfActiveAtoms.getActivity(lit2);
		double activity3 = heapOfActiveAtoms.getActivity(lit3);
		assertEquals(1d, activity1, DOUBLE_COMPARISON_EPSILON);
		assertEquals(1d, activity2, DOUBLE_COMPARISON_EPSILON);
		assertEquals(1d, activity3, DOUBLE_COMPARISON_EPSILON);
	}

	@Test
	public void testOneAtomHigherScore() {
		int lit1 = Literals.atomToLiteral(1);
		int lit2 = Literals.atomToLiteral(2);
		int lit3 = Literals.atomToLiteral(3);
		int lit1Neg = Literals.atomToLiteral(1, false);
		int lit2Neg = Literals.atomToLiteral(2, false);
		int lit3Neg = Literals.atomToLiteral(3, false);
		HeuristicTestUtils.addNoGoods(atomStore, assignment, noGoodStore, heapOfActiveAtoms, new NoGood(lit1, lit2Neg), new NoGood(lit1Neg, lit2),
				new NoGood(lit2, lit3Neg), new NoGood(lit2Neg, lit3));
		double activity1 = heapOfActiveAtoms.getActivity(lit1);
		double activity2 = heapOfActiveAtoms.getActivity(lit2);
		double activity3 = heapOfActiveAtoms.getActivity(lit3);
		assertEquals(0.25d, activity1, DOUBLE_COMPARISON_EPSILON);
		assertEquals(1d, activity2, DOUBLE_COMPARISON_EPSILON);
		assertEquals(0.25d, activity3, DOUBLE_COMPARISON_EPSILON);
	}

}
