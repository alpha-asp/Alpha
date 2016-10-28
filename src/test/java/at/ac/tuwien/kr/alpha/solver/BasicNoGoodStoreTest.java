package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.junit.Before;
import org.junit.Test;

import static at.ac.tuwien.kr.alpha.common.NoGood.fact;
import static at.ac.tuwien.kr.alpha.common.NoGood.headFirst;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;
import static org.junit.Assert.*;

public class BasicNoGoodStoreTest {
	private static final int DECISION_LEVEL = 0;

	private final BasicAssignment assignment;
	private final BasicNoGoodStore store;

	public BasicNoGoodStoreTest() {
		assignment = new BasicAssignment();
		store = new BasicNoGoodStore(assignment);
	}

	@Before
	public void setUp() {
		store.clear();
		store.setDecisionLevel(DECISION_LEVEL);
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
		assignment.assign(123, MBT, 0);
		assignment.assign(23, TRUE, 0);
		store.add(3, new NoGood(-123, 22, 23));
	}

	@Test
	public void assignment() {
		store.add(3, headFirst(-7, -4, -2));
		store.assign(4, TRUE);
		store.assign(2, FALSE);
		store.assign(7, FALSE);
		store.propagate();
		assertEquals(null, store.getViolatedNoGood());
	}


	@Test
	public void addNotCausingAssignment() {
		assignment.assign(1, TRUE, 0);
		store.add(3, headFirst(-3, -2, 1));

		assertEquals(null, assignment.getTruth(3));
	}

	@Test
	public void addNotCausingAssignmentUnassigned() {
		assignment.assign(1, TRUE, 0);
		store.add(3, headFirst(-5, 4, 1));

		assertEquals(null, assignment.getTruth(5));
	}

	@Test
	public void addNotCausingAssignmentFalse() {
		assignment.assign(1, FALSE, 0);
		assignment.assign(4, TRUE, 0);
		store.add(3, headFirst(-5, 4, -1));

		assertEquals(TRUE, assignment.getTruth(5));
	}

	@Test
	public void addNotCausingAssignmentTrue() {
		assignment.assign(1, TRUE, 0);
		assignment.assign(2, TRUE, 0);
		store.add(1, headFirst(-3, 2, -1));

		assertEquals(null, assignment.getTruth(3));
	}

	@Test
	public void propBinary() {
		assignment.assign(2, FALSE, DECISION_LEVEL);

		store.add(1, headFirst(-1, 2));
		store.propagate();

		assertEquals(null, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryFirstTrue() {
		assignment.assign(2, TRUE, DECISION_LEVEL);

		store.add(1, headFirst(-1, 2));
		store.propagate();
		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateBinarySecondTrue() {
		assignment.assign(1, FALSE, DECISION_LEVEL);

		store.add(1, new NoGood(new int[]{-1, 2}, 1));
		store.propagate();

		assertEquals(FALSE, assignment.getTruth(2));
	}

	@Test
	public void propagateBinaryMBT() {
		assignment.assign(2, MBT, DECISION_LEVEL);

		store.add(1, headFirst(-1, 2));
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryTrue() {
		assignment.assign(2, TRUE, DECISION_LEVEL);

		store.add(1, headFirst(-1, 2));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryMBTAfterAssignment() {
		store.add(1, headFirst(-1, 2));
		store.propagate();

		store.assign(2, MBT);
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryTrueAfterAssignment() {
		store.add(1, headFirst(-1, 2));
		store.assign(2, TRUE);
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateBinaryMBTTwice() {
		assignment.assign(2, MBT, DECISION_LEVEL);

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

		store.assign(2, MBT);

		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(3));
	}

	@Test
	public void propagateNaryTrue() {
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);

		assertTrue(store.add(1, headFirst(-1, 2, 3)));

		store.propagate();

		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateNaryFalse() {
		assignment.assign(2, FALSE, DECISION_LEVEL);
		assignment.assign(3, FALSE, DECISION_LEVEL);

		store.add(1, new NoGood(new int[]{-3, -2, 1}, 2));
		store.propagate();

		assertEquals(FALSE, assignment.getTruth(1));
	}

	@Test
	public void addFullyAssignedBinary() {
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);

		store.add(1, headFirst(-2, 3));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
	}

	@Test
	public void addFullyAssignedNary() {
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);
		assignment.assign(4, TRUE, DECISION_LEVEL);

		store.add(1, headFirst(-2, 3, 4));
		store.propagate();

		assertEquals(TRUE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(TRUE, assignment.getTruth(4));
	}

	@Test
	public void propagateNaryMBT() {
		final NoGood noGood = headFirst(-1, 2, 3);

		assignment.assign(2, MBT, DECISION_LEVEL);
		assignment.assign(3, MBT, DECISION_LEVEL);

		store.add(1, noGood);
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
	}

	@Test
	public void propagateNaryMBTTwice() {
		assignment.assign(4, FALSE, DECISION_LEVEL);
		assignment.assign(3, MBT, DECISION_LEVEL);
		assignment.assign(2, MBT, DECISION_LEVEL);

		store.add(1, headFirst(-1, 2, 3));
		store.add(2, headFirst(-5, -4, 1));
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(5));
	}

	@Test
	public void propagateNaryFactsMultiple() {
		NoGood[] noGoods = new NoGood[]{
			headFirst(-1, 2, 3),  // 1 <-  2, 3.
			headFirst(-5, -4, 1), // 5 <- -4, 1.
			fact(4),              // -4.
			fact(-3),             // 3.
			fact(-2)              // 2.
		};
		for (int i = 0; i < noGoods.length; i++) {
			assertTrue(store.add(i + 1, noGoods[i]));
		}

		// First deduce 1 from 2 and 3, then deduce
		// 5 from -4 and 1.
		assertTrue(store.propagate());

		assertEquals(TRUE, assignment.getTruth(1));
		assertEquals(TRUE, assignment.getTruth(5));
	}

	@Test
	public void moveThirdPointer() {
		store.add(1, headFirst(-1, 2, 3));
		store.propagate();
		store.add(2, fact(-2));
		store.propagate();
		assertEquals(null, assignment.getTruth(1));

		store.add(3, fact(-3));
		store.propagate();
		assertEquals(TRUE, assignment.getTruth(1));
	}

	@Test
	public void propagateNaryMBTTwiceReordered() {
		// From 2 and 3 follows 1.
		store.add(1, headFirst(-1, 2, 3));
		// From -4 and 1 follows 5.
		store.add(2, headFirst(-5, -4, 1));

		// Assign 4 to false (first premise for 5).
		store.assign(4, FALSE);

		// Assign 3 and 2 to MBT (premises for 1).
		store.assign(3, MBT);
		store.assign(2, MBT);

		// Now 1 must follow from 2 and 3,
		// and 5 must follow from -4 and 1.
		store.propagate();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(MBT, assignment.getTruth(5));
	}

	@Test
	public void conflictingFact() {
		final NoGood noGood = fact(1);
		assignment.assign(1, TRUE, DECISION_LEVEL);
		store.add(1, noGood);
		assertEquals(noGood, store.getViolatedNoGood());
	}

	@Test
	public void conflictingBinary() {
		final NoGood noGood = new NoGood(1, 2);
		assignment.assign(1, TRUE, DECISION_LEVEL);
		assignment.assign(2, TRUE, DECISION_LEVEL);
		store.add(1, noGood);
		assertEquals(noGood, store.getViolatedNoGood());
	}

	@Test
	public void conflictingNary() {
		final NoGood noGood = new NoGood(1, 2, 3);
		assignment.assign(1, TRUE, DECISION_LEVEL);
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);
		store.add(1, noGood);
		assertEquals(noGood, store.getViolatedNoGood());
	}

	@Test
	public void propagateViolatedConstraint() {
		assertTrue(store.add(1, headFirst(-3, -2, -1)));
		assertTrue(store.assign(1, FALSE));
		assertTrue(store.assign(2, FALSE));
		assertTrue(store.assign(3, FALSE));
		assertFalse(store.propagate());
		assertNotNull(store.getViolatedNoGood());
	}

	@Test
	public void noViolation() {
		assertTrue(store.add(1, headFirst(-7, -4, -2)));
		assertTrue(store.assign(4, TRUE));
		assertTrue(store.assign(2, FALSE));
		assertTrue(store.assign(7, FALSE));
		assertNull(store.getViolatedNoGood());
		store.propagate();
		assertNull(store.getViolatedNoGood());
	}
}