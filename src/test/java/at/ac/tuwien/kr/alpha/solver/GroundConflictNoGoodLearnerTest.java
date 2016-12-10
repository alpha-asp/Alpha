package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class GroundConflictNoGoodLearnerTest {

	private final Assignment assignment;
	private final NoGoodStore<ThriceTruth> store;

	public GroundConflictNoGoodLearnerTest() {
		this.assignment = new BasicAssignment();
		this.store = new BasicNoGoodStore(assignment);
	}

	@Test
	public void oneResolutionStep() {
		NoGood n1 = new NoGood(1, -2);
		NoGood n2 = new NoGood(2, 3);
		assignment.guess(1, ThriceTruth.TRUE);
		assignment.guess(3, ThriceTruth.TRUE);
		store.add(10, n1);
		store.propagate();
		assertEquals(assignment.get(2).getTruth(), ThriceTruth.MBT);
		store.add(11, n2);
		store.propagate();
		assertEquals(store.getViolatedNoGood(), n2);
		GroundConflictNoGoodLearner learner = new GroundConflictNoGoodLearner(assignment, store);
		learner.analyzeConflict(store.getViolatedNoGood());
		NoGood learnedNoGood = learner.obtainLearnedNoGood();
		assertEquals(learnedNoGood, new NoGood(1, 3));
		int backjumpingDecisionLevel = learner.computeBackjumpingDecisionLevel(learnedNoGood);
		assertEquals(backjumpingDecisionLevel, 1);
	}

	@Test
	public void resolveTreeLike() {
		NoGood n1 = new NoGood();
		NoGood n2 = new NoGood();
		NoGood n3 = new NoGood();
		NoGood n4 = new NoGood();
		// TODO: create test with three NoGoods that resolve tree-like.
	}



	// TODO: create test for 1UIP resolution.
}