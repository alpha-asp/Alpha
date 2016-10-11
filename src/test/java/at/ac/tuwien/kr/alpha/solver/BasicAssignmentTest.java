package at.ac.tuwien.kr.alpha.solver;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;
import static org.junit.Assert.*;

public class BasicAssignmentTest {
	private final BasicAssignment assignment;

	public BasicAssignmentTest() {
		assignment = new BasicAssignment();
	}

	@Before
	public void setUp() {
		assignment.clear();
	}

	@Test(expected = IllegalArgumentException.class)
	public void assign() throws Exception {
		assignment.assign(0, null, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeAtomThrows() throws Exception {
		assignment.assign(-1, null, 0);
	}

	@Test
	public void alreadyAssignedThrows() throws Exception {
		assertTrue(assignment.assign(1, MBT, 0));
		assertFalse(assignment.assign(1, FALSE, 0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void skippedDecisionLevelThrows() throws Exception {
		assignment.assign(1, MBT, 0);
		assignment.assign(1, TRUE, 2);
	}

	@Test
	public void initializeDecisionLevelState() throws Exception {
		assignment.assign(1, MBT, 0);
		assignment.assign(2, MBT, 1);
		assignment.assign(1, TRUE, 2);
	}

	@Test
	public void checkToString() {
		assignment.assign(1, FALSE, 0);
		assertEquals("[1=FALSE(0)]", assignment.toString());

		assignment.assign(2, TRUE, 0);
		assertEquals("[1=FALSE(0), 2=TRUE(0)]", assignment.toString());
	}

	@Test
	public void reassignGracefully() {
		assignment.assign(1, FALSE, 0);
		assignment.assign(1, FALSE, 0);
	}

	@Test
	public void assignAndBacktrack() {
		assignment.assign(1, MBT, 0);
		assignment.assign(2, FALSE, 0);
		assignment.assign(3, TRUE, 0);

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(FALSE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Collections.singletonList(3)));
		assertEquals(1, assignment.getMBTCount());

		assignment.assign(1, TRUE, 1);

		assertEquals(TRUE, assignment.getTruth(1));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Arrays.asList(3, 1)));
		assertEquals(0, assignment.getMBTCount());

		assignment.backtrack(0);

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(FALSE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Collections.singletonList(3)));
		assertEquals(1, assignment.getMBTCount());

		assignment.assign(4, MBT, 1);
		assignment.assign(5, MBT, 1);

		assertEquals(MBT, assignment.getTruth(4));
		assertEquals(MBT, assignment.getTruth(5));

		assignment.backtrack(0);

		assertFalse(assignment.isAssigned(4));
		assertFalse(assignment.isAssigned(5));
	}
}