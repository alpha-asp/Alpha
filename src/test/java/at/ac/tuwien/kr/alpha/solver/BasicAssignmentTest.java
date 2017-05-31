package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
		assignment.assign(0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeAtomThrows() throws Exception {
		assignment.assign(-1, null);
	}

	@Test
	public void alreadyAssignedThrows() throws Exception {
		assertTrue(assignment.assign(1, MBT));
		assertFalse(assignment.assign(1, FALSE));
	}

	@Test
	public void initializeDecisionLevelState() throws Exception {
		assignment.assign(1, MBT);
		assignment.guess(2, MBT);
		assignment.guess(1, TRUE);
	}

	@Test
	public void checkToString() {
		assignment.assign(1, FALSE);
		assertEquals("[F_1@0]", assignment.toString());
		//assertEquals("[1=FALSE(0)]", assignment.toString());

		assignment.assign(2, TRUE);
		assertEquals("[F_1@0, T_2@0]", assignment.toString());
		// assertEquals("[1=FALSE(0), 2=TRUE(0)]", assignment.toString());
	}

	@Test
	public void reassignGracefully() {
		assignment.assign(1, FALSE);
		assignment.assign(1, FALSE);
	}

	@Test
	public void assignAndBacktrack() {
		assignment.assign(1, MBT);
		assignment.assign(2, FALSE);
		assignment.assign(3, TRUE);

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(FALSE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Collections.singletonList(3)));
		assertEquals(1, assignment.getMBTCount());

		assignment.guess(1, TRUE);

		assertEquals(TRUE, assignment.getTruth(1));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Arrays.asList(3, 1)));
		assertEquals(0, assignment.getMBTCount());

		assignment.backtrack();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(FALSE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Collections.singletonList(3)));
		assertEquals(1, assignment.getMBTCount());

		assignment.guess(4, MBT);
		assignment.assign(5, MBT);

		assertEquals(MBT, assignment.getTruth(4));
		assertEquals(MBT, assignment.getTruth(5));

		assignment.backtrack();

		assertFalse(assignment.isAssigned(4));
		assertFalse(assignment.isAssigned(5));

		assignment.guess(4, TRUE);

		assertEquals(TRUE, assignment.getTruth(4));

		assignment.backtrack();

		assertNull(assignment.getTruth(4));
	}

	@Test
	public void testContains() {
		assignment.assign(1, TRUE);
		assertTrue(assignment.containsWeakComplement(+1));
		assertFalse(assignment.containsWeakComplement(-1));

		assignment.assign(2, FALSE);
		assertTrue(assignment.containsWeakComplement(-2));
		assertFalse(assignment.containsWeakComplement(+2));

		assignment.assign(1, MBT);
		assertTrue(assignment.containsWeakComplement(+1));
		assertFalse(assignment.containsWeakComplement(-1));
	}

	@Test
	public void assignmentsToProcess() throws Exception {
		assignment.assign(1, MBT);

		Queue<ReadableAssignment.Entry> queue = assignment.getAssignmentsToProcess();
		assertEquals(1, queue.remove().getAtom());

		assignment.guess(2, MBT);
		assignment.guess(1, TRUE);

		assertEquals(2, queue.remove().getAtom());
		assertEquals(1, queue.element().getAtom());

		queue = assignment.getAssignmentsToProcess();
		assertEquals(1, queue.remove().getAtom());
	}

	@Test
	public void newAssignmentsIteratorAndBacktracking() throws Exception {

		Iterator<ReadableAssignment.Entry> newAssignmentsIterator;

		assignment.assign(1, MBT);
		assignment.guess(2, MBT);

		newAssignmentsIterator = assignment.getNewAssignmentsIterator();

		assertEquals(1, newAssignmentsIterator.next().getAtom());
		assertEquals(2, newAssignmentsIterator.next().getAtom());
		assertFalse(newAssignmentsIterator.hasNext());


		assignment.guess(1, TRUE);
		assignment.backtrack();
		assignment.assign(3, FALSE);

		newAssignmentsIterator = assignment.getNewAssignmentsIterator();
		assertEquals(3, newAssignmentsIterator.next().getAtom());
		assertFalse(newAssignmentsIterator.hasNext());
	}

	@Test
	public void newAssignmentsIteratorLowerDecisionLevelAndBacktracking() throws Exception {

		Iterator<ReadableAssignment.Entry> newAssignmentsIterator;

		assignment.guess(1, MBT);
		assignment.guess(2, MBT);
		assignment.assign(3, MBT, null, 1);
		assignment.backtrack();

		newAssignmentsIterator = assignment.getNewAssignmentsIterator();
		assertEquals(1, newAssignmentsIterator.next().getAtom());
		assertEquals(3, newAssignmentsIterator.next().getAtom());
		assertFalse(newAssignmentsIterator.hasNext());
	}

	@Test
	public void iteratorAndBacktracking() throws Exception {
		Queue<ReadableAssignment.Entry> assignmentsToProcess = assignment.getAssignmentsToProcess();

		assignment.assign(1, MBT);
		assertEquals(1, assignmentsToProcess.remove().getAtom());

		assignment.guess(2, MBT);
		assertEquals(2, assignmentsToProcess.remove().getAtom());

		assignment.guess(1, TRUE);
		assertEquals(1, assignmentsToProcess.remove().getAtom());

		assignment.backtrack();

		assignment.assign(3, FALSE);
		assertEquals(3, assignmentsToProcess.remove().getAtom());
	}

	@Test
	public void mbtCounterAssignMbtToFalseOnLowerDecisionLevel() {
		assertTrue(assignment.guess(1, TRUE));
		assertTrue(assignment.guess(2, FALSE));

		assertTrue(assignment.assign(3, MBT, null, 2));
		assertEquals(1, assignment.getMBTCount());

		assertTrue(assignment.guess(4, TRUE));

		assertFalse(assignment.assign(3, FALSE, null, 1));

		assignment.backtrack();
		assignment.backtrack();

		assertEquals(0, assignment.getMBTCount());
	}
}