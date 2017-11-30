package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;
import org.junit.Before;
import org.junit.Test;

import static at.ac.tuwien.kr.alpha.common.NoGood.fact;
import static at.ac.tuwien.kr.alpha.common.NoGood.headFirst;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;
import static org.junit.Assert.*;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class NoGoodStoreAlphaRoamingTest {

	private final ArrayAssignment assignment;
	private final NoGoodStoreAlphaRoaming store;

	public NoGoodStoreAlphaRoamingTest() {
		assignment = new ArrayAssignment(null);
		store = new NoGoodStoreAlphaRoaming(assignment);
	}

	@Before
	public void setUp() {
		store.clear();
		assignment.growForMaxAtomId(200);
	}

	@Test
	public void singleFact() {
		store.add(1, fact(-1));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void single() {
		store.add(1, new NoGood(-1));
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void constraintWithAssignment() {
		assignment.assign(123, MBT);
		assignment.assign(23, TRUE);
		store.add(3, new NoGood(-123, 22, 23));
	}

	@Test
	public void assignment() {
		store.add(3, headFirst(-7, -4, -2));
		assignment.assign(4, TRUE);
		assignment.assign(2, FALSE);
		assignment.assign(7, FALSE);
		assertNull(store.propagate());
	}


	@Test
	public void addNotCausingAssignment() {
		assignment.assign(1, TRUE);
		store.add(3, headFirst(-3, -2, 1));

		assertEquals(null, assignment.getTruth(3));
	}

	@Test
	public void addNotCausingAssignmentUnassigned() {
		assignment.assign(1, TRUE);
		store.add(3, headFirst(-5, 4, 1));

		assertEquals(null, assignment.getTruth(5));
	}

	@Test
	public void addNotCausingAssignmentFalse() {
		assignment.assign(1, FALSE);
		assignment.assign(4, TRUE);
		store.add(3, headFirst(-5, 4, -1));

		assertEquals(TRUE, assignment.getTruth(5));
	}

	@Test
	public void addNotCausingAssignmentTrue() {
		assignment.assign(1, TRUE);
		assignment.assign(2, TRUE);
		store.add(1, headFirst(-3, 2, -1));

		assertEquals(null, assignment.getTruth(3));
	}

	@Test
	public void propBinary() {
		assignment.assign(2, FALSE);

		store.add(1, headFirst(-1, 2));
		store.propagate();

		assertEquals(null, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryFirstTrue() {
		assignment.assign(2, TRUE);

		store.add(1, headFirst(-1, 2));
		store.propagate();
		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryMBT() {
		assignment.assign(2, MBT);

		store.add(1, headFirst(-1, 2));
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryTrue() {
		assignment.assign(2, TRUE);

		store.add(1, headFirst(-1, 2));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryMBTAfterAssignment() {
		store.add(1, headFirst(-1, 2));
		store.propagate();

		assignment.assign(2, MBT);
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryTrueAfterAssignment() {
		store.add(1, headFirst(-1, 2));
		assignment.assign(2, TRUE);
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryMBTTwice() {
		assignment.assign(2, MBT);

		store.add(1, new NoGood(-1, 2));
		store.add(2, new NoGood(-3, 1));

		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(3));
	}


	@Test
	public void propagateBinaryMBTTwiceOutofSync() {
		store.add(1, new NoGood(-1, 2));
		store.add(2, new NoGood(-3, 1));

		assignment.assign(2, MBT);

		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(3));
	}

	@Test
	public void propagateNaryTrue() {
		assignment.assign(2, TRUE);
		assignment.assign(3, TRUE);

		assertNull(store.add(1, headFirst(-1, 2, 3)));

		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateNaryFalse() {
		assignment.assign(2, FALSE);
		assignment.assign(3, FALSE);

		store.add(1, headFirst(-1, -3, -2));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void addFullyAssignedBinary() {
		assignment.assign(2, TRUE);
		assignment.assign(3, TRUE);

		store.add(1, headFirst(-2, 3));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
	}

	@Test
	public void addFullyAssignedNary() {
		assignment.assign(2, TRUE);
		assignment.assign(3, TRUE);
		assignment.assign(4, TRUE);

		store.add(1, headFirst(-2, 3, 4));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(TRUE, assignment.getTruth(4));
	}

	@Test
	public void propagateNaryMBT() {
		final NoGood noGood = headFirst(-1, 2, 3);

		assignment.assign(2, MBT);
		assignment.assign(3, MBT);

		store.add(1, noGood);
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void propagateNaryMBTTwice() {
		assignment.assign(4, FALSE);
		assignment.assign(3, MBT);
		assignment.assign(2, MBT);

		store.add(1, headFirst(-1, 2, 3));
		assertEquals(MBT, assignment.getTruth(1));

		store.add(2, headFirst(-5, -4, 1));
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(5));
	}

	@Test
	public void propagateNaryFactsMultiple() {
		NoGood[] noGoods = new NoGood[]{
			headFirst(-1, 2, 3), // 1 <- 2, 3.
			headFirst(-5, 4, 1), // 5 <- 4, 1.
			fact(-4),            // 4.
			fact(-3),            // 3.
			fact(-2)             // 2.
		};
		for (int i = 0; i < noGoods.length; i++) {
			assertNull(store.add(i + 1, noGoods[i]));
		}

		// First deduce 1 from 2 and 3, then deduce
		// 5 from -4 and 1.
		assertNull(store.propagate());
		assertTrue(store.didPropagate());

		assertEquals(TRUE, assignment.getTruth(1));
		assertEquals(TRUE, assignment.getTruth(5));
	}

	@Test
	public void moveThirdPointer() {
		// 1 <- 2, 3.
		store.add(1, headFirst(-1, 2, 3));
		assertNull(store.propagate());
		assertFalse(store.didPropagate());

		// 2.
		store.add(2, fact(-2));
		assertNull(store.propagate());
		assertFalse(store.didPropagate());
		assertNull(assignment.getTruth(1));

		// 3.
		store.add(3, fact(-3));
		assertNull(store.propagate());
		assertTrue(store.didPropagate());

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateNaryMBTTwiceReordered() {
		// From 2 and 3 follows 1.
		store.add(1, headFirst(-1, 2, 3));
		// From -4 and 1 follows 5.
		store.add(2, headFirst(-5, -4, 1));

		// Assign 4 to false (first premise for 5).
		assignment.assign(4, FALSE);

		// Assign 3 and 2 to MBT (premises for 1).
		assignment.assign(3, MBT);
		assignment.assign(2, MBT);

		// Now 1 must follow from 2 and 3,
		// and 5 must follow from -4 and 1.
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(5));
	}

	@Test
	public void conflictingFact() {
		final NoGood noGood = fact(-1);
		assignment.assign(1, FALSE);
		ConflictCause conflictCause = store.add(1, noGood);
		assertNotNull(conflictCause);
		assertEquals(noGood, conflictCause.getViolatedNoGood());
	}

	@Test
	public void conflictingBinary() {
		final NoGood noGood = new NoGood(1, 2);
		assignment.assign(1, TRUE);
		assignment.assign(2, TRUE);
		ConflictCause conflictCause = store.add(1, noGood);
		assertEquals(noGood, conflictCause.getViolatedNoGood());
	}

	@Test
	public void conflictingNary() {
		final NoGood noGood = new NoGood(1, 2, 3);
		assignment.assign(1, TRUE);
		assignment.assign(2, TRUE);
		assignment.assign(3, TRUE);
		ConflictCause conflictCause = store.add(1, noGood);
		assertEquals(noGood, conflictCause.getViolatedNoGood());
	}

	@Test
	public void propagateViolatedConstraint() {
		NoGood noGood = headFirst(-3, -2, -1);
		assertNull(store.add(1, noGood));
		assertNull(assignment.assign(1, FALSE));
		assertNull(assignment.assign(2, FALSE));
		assertNull(assignment.assign(3, FALSE));
		ConflictCause conflictCause = store.propagate();
		assertNotNull(conflictCause);
		assertFalse(store.didPropagate());
		//assertEquals(noGood, conflictCause.getViolatedNoGood());
	}

	@Test
	public void noViolation() {
		assertNull(store.add(1, headFirst(-7, -4, -2)));
		assertNull(assignment.assign(4, TRUE));
		assertNull(assignment.assign(2, FALSE));
		assertNull(assignment.assign(7, FALSE));
		assertNull(store.propagate());
	}

	@Test
	public void propagateViolatedConstraintHeadless() {
		NoGood noGood = new NoGood(3, 11, 19);
		assertNull(store.add(24, noGood));
		assertNull(assignment.assign(3, TRUE));
		assertNull(assignment.assign(11, TRUE));
		assertNull(assignment.assign(19, TRUE));
		ConflictCause conflictCause = store.propagate();
		assertFalse(store.didPropagate());
		assertNotNull(conflictCause);
		//assertEquals(noGood, conflictCause.getViolatedNoGood());
	}

	@Test
	public void propagateViolatedConstraintHeadlessMbt() {
		NoGood noGood = new NoGood(3, 11, 19);
		assertNull(store.add(24, noGood));
		assertNull(assignment.assign(3, MBT));
		assertNull(assignment.assign(11, MBT));
		assertNull(assignment.assign(19, MBT));
		ConflictCause conflictCause = store.propagate();
		assertFalse(store.didPropagate());
		assertNotNull(conflictCause);
		//assertEquals(noGood, conflictCause.getViolatedNoGood());
	}

	@Test
	public void neverViolatedNoGood() {
		NoGood noGood = new NoGood(-44, 10, 13, 44);
		assertNull(store.add(80, noGood));
		assertNull(assignment.assign(10, TRUE));
		assertNull(assignment.assign(13, TRUE));
		assertNull(assignment.assign(44, FALSE));
		assertNull(store.propagate());
	}

	@Test
	public void naryNoGoodViolatedAfterAddition() {
		NoGood noGood = new NoGood(1, 2, 3);
		assertNull(store.add(11, noGood));
		assertNull(assignment.assign(1, MBT));
		assertNull(assignment.assign(2, MBT));
		assertNull(assignment.assign(3, MBT));
		assertNotNull(store.propagate());
	}

	@Test
	public void naryNoGoodViolatedDuringAdditionAllTrue() {
		NoGood noGood = new NoGood(1, 2, 3);
		assertNull(assignment.assign(1, TRUE));
		assertNull(assignment.assign(2, TRUE));
		assertNull(assignment.assign(3, TRUE));
		ConflictCause conflictCause = store.add(11, noGood);
		assertNotNull(conflictCause);
		assertNotNull(conflictCause.getViolatedNoGood());
	}

	@Test
	public void naryNoGoodViolatedDuringAdditionAllMbt() {
		NoGood noGood = new NoGood(1, 2, 3);
		assertNull(assignment.assign(1, MBT));
		assertNull(assignment.assign(2, MBT));
		assertNull(assignment.assign(3, MBT));
		ConflictCause conflictCause = store.add(11, noGood);
		assertNotNull(conflictCause);
		assertNotNull(conflictCause.getViolatedNoGood());
	}

	@Test
	public void binaryNoGoodViolatedAfterAddition() {
		NoGood noGood = new NoGood(1, 2);
		assertNull(store.add(11, noGood));
		assertNull(assignment.assign(1, MBT));
		assertNull(assignment.assign(2, MBT));
		assertNotNull(store.propagate());
	}

	@Test
	public void binaryNoGoodViolatedDuringAdditionAllTrue() {
		NoGood noGood = new NoGood(1, 2);
		assertNull(assignment.assign(1, TRUE));
		assertNull(assignment.assign(2, TRUE));
		ConflictCause conflictCause = store.add(11, noGood);
		assertNotNull(conflictCause);
		assertNotNull(conflictCause.getViolatedNoGood());
	}

	@Test
	public void binaryNoGoodViolatedDuringAdditionAllMbt() {
		NoGood noGood = new NoGood(1, 2);
		assertNull(assignment.assign(1, MBT));
		assertNull(assignment.assign(2, MBT));
		ConflictCause conflictCause = store.add(11, noGood);
		assertNotNull(conflictCause);
		assertNotNull(conflictCause.getViolatedNoGood());
	}

	@Test
	public void addedViolatedBinaryNoGoodPropagatesAfterBacktracking() {
		NoGood noGood = new NoGood(70, 195);
		assertNull(assignment.choose(70, MBT));
		assertNull(assignment.choose(195, MBT));
		assertNotNull(store.add(3, noGood));
		assignment.backtrack();
		assertNull(store.add(3, noGood));
		store.propagate();
		assertTrue(FALSE.equals(assignment.getTruth(195)));
	}

	@Test
	public void addedViolatedNaryNoGoodPropagatesAfterBacktracking() {
		NoGood noGood = new NoGood(70, 195, 36);
		assertNull(assignment.choose(70, MBT));
		assertNull(assignment.choose(195, MBT));
		assertNull(assignment.choose(36, MBT));
		assertNotNull(store.add(3, noGood));
		assignment.backtrack();
		assertNull(store.add(3, noGood));
		store.propagate();
		assertTrue(FALSE.equals(assignment.getTruth(36)));
	}

	@Test
	public void binaryNoGoodPropagatesTrueFromFalse() {
		NoGood noGood = headFirst(-11, -12);
		assertNull(store.add(5, noGood));
		assertNull(assignment.choose(12, FALSE));
		store.propagate();
		assertTrue(TRUE.equals(assignment.getTruth(11)));
	}

	@Test
	public void binaryNoGoodPropagatesTrueFromTrue() {
		NoGood noGood = headFirst(-11, 12);
		assertNull(store.add(5, noGood));
		assertNull(assignment.choose(12, TRUE));
		store.propagate();
		assertTrue(TRUE.equals(assignment.getTruth(11)));
	}


	@Test
	public void addedBinaryNoGoodPropagatesTrueFromFalse() {
		NoGood noGood = headFirst(-11, -12);
		assertNull(assignment.choose(12, FALSE));
		assertNull(store.add(5, noGood));
		assertTrue(TRUE.equals(assignment.getTruth(11)));
	}

	@Test
	public void addedBinaryNoGoodPropagatesTrueFromTrue() {
		NoGood noGood = headFirst(-11, 12);
		assertNull(assignment.choose(12, TRUE));
		assertNull(store.add(5, noGood));
		store.propagate();
		assertTrue(TRUE.equals(assignment.getTruth(11)));
	}

	@Test
	public void naryNoGoodPropagatesTrueFromFalse() {
		NoGood noGood = headFirst(-1, 2, -3);
		assertNull(store.add(10, noGood));
		assertNull(assignment.assign(2, TRUE));
		store.propagate();
		assertNull(assignment.assign(3, FALSE));
		store.propagate();
		assertTrue(TRUE.equals(assignment.getTruth(1)));
	}

	@Test
	public void naryNoGoodPropagatesTrueFromTrue() {
		NoGood noGood = headFirst(-1, 2, -3);
		assertNull(store.add(10, noGood));
		assertNull(assignment.assign(3, FALSE));
		store.propagate();
		assertNull(assignment.assign(2, TRUE));
		store.propagate();
		assertTrue(TRUE.equals(assignment.getTruth(1)));
	}

	@Test
	public void propagationAtLowerDecisionLevel() {
		NoGood noGood = headFirst(-1, 2, -3);
		assertNull(assignment.choose(3, FALSE));
		assertNull(assignment.choose(2, TRUE));
		assertNull(assignment.choose(4, TRUE));
		assertNull(store.add(10, noGood));
		store.propagate();
		assertTrue(TRUE.equals(assignment.getTruth(1)));
		Assignment.Entry entry = assignment.get(1);
		assertTrue(TRUE.equals(entry.getTruth()));
		assertEquals(2, entry.getDecisionLevel());
	}

}