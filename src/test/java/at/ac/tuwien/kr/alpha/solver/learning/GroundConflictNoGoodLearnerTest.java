package at.ac.tuwien.kr.alpha.solver.learning;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class GroundConflictNoGoodLearnerTest {

	private final Assignment assignment;
	private final NoGoodStore store;

	public GroundConflictNoGoodLearnerTest() {
		this.assignment = new BasicAssignment();
		this.store = new BasicNoGoodStore(assignment);
	}

	@Test
	public void smallConflictNonTrivial1UIP() {
		GroundConflictNoGoodLearner learner = new GroundConflictNoGoodLearner(assignment);

		NoGood n1 = new NoGood(2, -8, 1);
		NoGood n2 = new NoGood(-1, -7);
		NoGood n3 = new NoGood(-3, 1);
		NoGood n4 = new NoGood(5, 3);
		NoGood n5 = new NoGood(6, -5);
		NoGood n6 = new NoGood(4, -2);
		NoGood n7 = new NoGood(-6, -4);
		store.add(10, n1);
		store.add(11, n2);
		store.add(12, n3);
		store.add(13, n4);
		store.add(14, n5);
		store.add(15, n6);
		store.add(16, n7);

		assignment.guess(9, ThriceTruth.TRUE);
		assignment.guess(8, ThriceTruth.FALSE);
		assertFalse(store.propagate());
		assignment.guess(7, ThriceTruth.FALSE);
		assertEquals(true, store.propagate());

		NoGood violatedNoGood = store.getViolatedNoGood();
		assertNotNull(violatedNoGood);
		assertTrue(violatedNoGood.equals(n5) || violatedNoGood.equals(n7));
		GroundConflictNoGoodLearner.ConflictAnalysisResult analysisResult = learner.analyzeConflictingNoGood(violatedNoGood);
		NoGood learnedNoGood = analysisResult.learnedNoGood;
		assertEquals(new NoGood(1, -8), learnedNoGood);
		int backjumpingDecisionLevel = analysisResult.backjumpLevel;
		assertEquals(backjumpingDecisionLevel, 2);
		assertFalse(analysisResult.clearLastGuessAfterBackjump);
	}

	@Test
	public void subCurrentDLPropagationWithGuessCauseOfConflict() {
		NoGood n1 = new NoGood(1, -2);
		NoGood n2 = new NoGood(2, 3);
		store.add(10, n1);
		assignment.guess(1, ThriceTruth.TRUE);
		assignment.guess(3, ThriceTruth.TRUE);
		store.propagate();
		assertEquals(ThriceTruth.MBT, assignment.get(2).getTruth());
		assertEquals(1, assignment.get(2).getDecisionLevel());
		NoGoodStore.ConflictCause conflictCause = store.add(11, n2);
		assertNotNull(conflictCause);
		//store.propagate();
		assertNotNull(conflictCause.getViolatedGuess());
		assertEquals(3, conflictCause.getViolatedGuess().getAtom());
		assertEquals(2, conflictCause.getViolatedGuess().getDecisionLevel());
		assertNull(conflictCause.getViolatedNoGood());
	}
}