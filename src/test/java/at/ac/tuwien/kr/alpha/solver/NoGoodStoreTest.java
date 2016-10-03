package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.common.NoGood.fact;
import static at.ac.tuwien.kr.alpha.common.NoGood.headFirst;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;
import static org.junit.Assert.assertEquals;

public class NoGoodStoreTest {
	private static final int DECISION_LEVEL = 0;

	private final Assignment<ThriceTruth> assignment;
	private final NoGoodStore store;

	public NoGoodStoreTest() {
		assignment = new Assignment<>();
		store = new NoGoodStore(assignment);
	}

	@Before
	public void setUp() {
		store.clear();
		store.setDecisionLevel(DECISION_LEVEL);
	}

	@Test
	public void singleFact() {
		store.add(fact(-1));
		store.propagate();

		assertEquals(TRUE, assignment.get(1));
	}

	@Test
	public void single() {
		store.add(new NoGood(-1));
		store.propagate();

		assertEquals(MBT, assignment.get(1));
	}

	@Test
	public void propBinary() {
		assignment.assign(2, FALSE, DECISION_LEVEL);

		store.add(headFirst(-1, 2));
		store.propagate();

		assertEquals(null, assignment.get(1));
	}

	@Test
	@Ignore
	public void propagateBinaryFirstTrue() {
		assignment.assign(2, TRUE, DECISION_LEVEL);

		store.add(headFirst(-1, 2));
		store.propagate();

		assertEquals(TRUE, assignment.get(1));
	}

	@Test
	public void propagateBinarySecondTrue() {
		assignment.assign(1, FALSE, DECISION_LEVEL);

		store.add(new NoGood(new int[]{-1, 2}, 1));
		store.propagate();

		assertEquals(FALSE, assignment.get(2));
	}

	@Test
	public void propagateBinaryMBT() {
		assignment.assign(2, MBT, DECISION_LEVEL);

		store.add(headFirst(-1, 2));
		store.propagate();

		assertEquals(MBT, assignment.get(1));
	}

	@Test
	public void propagateBinaryTrue() {
		assignment.assign(2, TRUE, DECISION_LEVEL);

		store.add(headFirst(-1, 2));
		store.propagate();

		assertEquals(TRUE, assignment.get(1));
	}

	@Test
	public void propagateBinaryMBTAfterAssignment() {
		store.add(headFirst(-1, 2));
		store.propagate();

		store.assign(2, MBT);
		store.propagate();

		assertEquals(MBT, assignment.get(1));
	}

	@Test
	public void propagateBinaryTrueAfterAssignment() {
		store.add(headFirst(-1, 2));
		store.assign(2, TRUE);
		store.propagate();

		assertEquals(TRUE, assignment.get(1));
	}

	@Test
	public void propagateBinaryMBTTwice() {
		assignment.assign(2, MBT, DECISION_LEVEL);

		store.add(new NoGood(-1, 2));
		store.add(new NoGood(-3, 1));

		store.propagate();

		assertEquals(MBT, assignment.get(1));
		assertEquals(MBT, assignment.get(3));
	}


	@Test
	public void propagateBinaryMBTTwiceOutofSync() {
		store.add(new NoGood(-1, 2));
		store.add(new NoGood(-3, 1));

		store.assign(2, MBT);

		store.propagate();

		assertEquals(MBT, assignment.get(1));
		assertEquals(MBT, assignment.get(3));
	}

	@Test
	@Ignore
	public void propagateNaryTrue() {
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);

		store.add(headFirst(-1, 2, 3));
		store.propagate();

		assertEquals(TRUE, assignment.get(1));
	}

	@Test
	public void propagateNaryFalse() {
		assignment.assign(2, FALSE, DECISION_LEVEL);
		assignment.assign(3, FALSE, DECISION_LEVEL);

		store.add(new NoGood(new int[]{-3, -2, 1}, 2));
		store.propagate();

		assertEquals(FALSE, assignment.get(1));
	}

	@Test
	public void addFullyAssignedBinary() {
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);

		store.add(headFirst(-2, 3));
		store.propagate();

		assertEquals(TRUE, assignment.get(2));
		assertEquals(TRUE, assignment.get(3));
	}

	@Test
	public void addFullyAssignedNary() {
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);
		assignment.assign(4, TRUE, DECISION_LEVEL);

		store.add(headFirst(-2, 3, 4));
		store.propagate();

		assertEquals(TRUE, assignment.get(2));
		assertEquals(TRUE, assignment.get(3));
		assertEquals(TRUE, assignment.get(4));
	}

	@Test
	public void propagateNaryMBT() {
		final NoGood noGood = headFirst(-1, 2, 3);

		assignment.assign(2, MBT, DECISION_LEVEL);
		assignment.assign(3, MBT, DECISION_LEVEL);

		store.add(noGood);
		store.propagate();

		assertEquals(MBT, assignment.get(1));
	}

	@Test
	public void propagateNaryMBTTwice() {
		assignment.assign(4, FALSE, DECISION_LEVEL);
		assignment.assign(3, MBT, DECISION_LEVEL);
		assignment.assign(2, MBT, DECISION_LEVEL);

		store.add(headFirst(-1, 2, 3));
		store.add(headFirst(-5, -4, 1));
		store.propagate();

		assertEquals(MBT, assignment.get(1));
		assertEquals(MBT, assignment.get(5));
	}

	@Test
	@Ignore
	public void propagateNaryFactsMultiple() {
		Stream.of(
			headFirst(-1, 2, 3),
			headFirst(-5, -4, 1),
			fact(4),
			fact(-3),
			fact(-2)
		)
		.forEach(store::add);
		store.propagate();

		assertEquals(TRUE, assignment.get(1));
		assertEquals(TRUE, assignment.get(5));
	}

	@Test
	public void moveThirdPointer() {
		store.add(headFirst(-1, 2, 3));
		store.propagate();
		store.add(fact(-2));
		store.propagate();
		assertEquals(null, assignment.get(1));

		System.out.println("NOW FOR THE REEL SHIZ");

		store.add(fact(-3));
		store.propagate();
		assertEquals(TRUE, assignment.get(1));
	}

	@Test
	public void propagateNaryMBTTwiceReordered() {
		// From 2 and 3 follows 1.
		store.add(headFirst(-1, 2, 3));
		// From -4 and 1 follows 5.
		store.add(headFirst(-5, -4, 1));

		// Assign 4 to false (first premise for 5).
		store.assign(4, FALSE);

		// Assign 3 and 2 to MBT (premises for 1).
		store.assign(3, MBT);
		store.assign(2, MBT);

		// Now 1 must follow from 2 and 3,
		// and 5 must follow from -4 and 1.
		store.propagate();

		assertEquals(MBT, assignment.get(1));
		assertEquals(MBT, assignment.get(5));
	}

	@Test(expected = ConflictingNoGoodException.class)
	public void conflictingFact() {
		assignment.assign(1, TRUE, DECISION_LEVEL);
		store.add(fact(1));
	}

	@Test(expected = ConflictingNoGoodException.class)
	public void conflictingBinary() {
		assignment.assign(1, TRUE, DECISION_LEVEL);
		assignment.assign(2, TRUE, DECISION_LEVEL);
		store.add(new NoGood(1, 2));
	}

	@Test(expected = ConflictingNoGoodException.class)
	public void conflictingNary() {
		assignment.assign(1, TRUE, DECISION_LEVEL);
		assignment.assign(2, TRUE, DECISION_LEVEL);
		assignment.assign(3, TRUE, DECISION_LEVEL);
		store.add(new NoGood(1, 2, 3));
	}
}