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

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.AtomStoreTest;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.*;
import org.junit.Ignore;
import org.junit.Test;

import static at.ac.tuwien.kr.alpha.common.NoGoodTest.fromOldLiterals;
import static org.junit.Assert.*;

/**
 * Copyright (c) 2016-2019, the Alpha Team.
 */
public class GroundConflictNoGoodLearnerTest {

	private final WritableAssignment assignment;
	private final NoGoodStore store;

	public GroundConflictNoGoodLearnerTest() {
		AtomStore atomStore = new AtomStoreImpl(true);
		AtomStoreTest.fillAtomStore(atomStore, 20);
		this.assignment = new TrailAssignment(atomStore);
		this.assignment.growForMaxAtomId();
		this.store = new NoGoodStoreAlphaRoaming(assignment);
		this.store.growForMaxAtomId(20);
	}

	@Test
	public void smallConflictNonTrivial1UIP() {
		GroundConflictNoGoodLearner learner = new GroundConflictNoGoodLearner(assignment);

		NoGood n1 = new NoGood(fromOldLiterals(2, -8, 1));
		NoGood n2 = new NoGood(fromOldLiterals(-1, -7));
		NoGood n3 = new NoGood(fromOldLiterals(-3, 1));
		NoGood n4 = new NoGood(fromOldLiterals(5, 3));
		NoGood n5 = new NoGood(fromOldLiterals(6, -5));
		NoGood n6 = new NoGood(fromOldLiterals(4, -2));
		NoGood n7 = new NoGood(fromOldLiterals(-6, -4));
		store.add(10, n1);
		store.add(11, n2);
		store.add(12, n3);
		store.add(13, n4);
		store.add(14, n5);
		store.add(15, n6);
		store.add(16, n7);

		assignment.choose(9, ThriceTruth.TRUE);
		assignment.choose(8, ThriceTruth.FALSE);
		assertNull(store.propagate());
		assertFalse(store.didPropagate());
		assignment.choose(7, ThriceTruth.FALSE);
		ConflictCause conflictCause = store.propagate();
		assertTrue(store.didPropagate());

		assertNotNull(conflictCause);
		NoGood violatedNoGood = conflictCause.getViolatedNoGood();
		assertNotNull(violatedNoGood);
		assertTrue(violatedNoGood.equals(n5) || violatedNoGood.equals(n7));
		GroundConflictNoGoodLearner.ConflictAnalysisResult analysisResult = learner.analyzeConflictingNoGood(violatedNoGood);
		NoGood learnedNoGood = analysisResult.learnedNoGood;
		assertEquals(new NoGood(fromOldLiterals(1, -8)), learnedNoGood);
		int backjumpingDecisionLevel = analysisResult.backjumpLevel;
		assertEquals(backjumpingDecisionLevel, 2);
		assertFalse(analysisResult.clearLastChoiceAfterBackjump);
	}

	@Ignore // TrailAssignment no longer propagates at lower decision level.
	@Test
	public void subCurrentDLPropagationWithChoiceCauseOfConflict() {
		GroundConflictNoGoodLearner learner = new GroundConflictNoGoodLearner(assignment);
		NoGood n1 = new NoGood(fromOldLiterals(1, -2));
		NoGood n2 = new NoGood(fromOldLiterals(2, 3));
		store.add(10, n1);
		assignment.choose(1, ThriceTruth.TRUE);
		assignment.choose(3, ThriceTruth.TRUE);
		store.propagate();
		assertEquals(ThriceTruth.MBT, assignment.get(2).getTruth());
		assertEquals(1, assignment.get(2).getDecisionLevel());
		ConflictCause conflictCause = store.add(11, n2);
		assertNotNull(conflictCause);
		assertNotNull(conflictCause.getViolatedNoGood());
		GroundConflictNoGoodLearner.ConflictAnalysisResult conflictAnalysisResult = learner.analyzeConflictingNoGood(conflictCause.getViolatedNoGood());
		assertNull(conflictAnalysisResult.learnedNoGood);
		assertEquals(2, conflictAnalysisResult.backjumpLevel);

	}
}