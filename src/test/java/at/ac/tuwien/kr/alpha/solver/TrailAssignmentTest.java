package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.AtomStoreTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;
import static org.junit.Assert.*;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class TrailAssignmentTest {
	private final TrailAssignment assignment;

	public TrailAssignmentTest() {
		AtomStore atomStore = new AtomStoreImpl();
		AtomStoreTest.fillAtomStore(atomStore, 20);
		assignment = new TrailAssignment(atomStore);
	}

	@Before
	public void setUp() {
		assignment.clear();
		assignment.growForMaxAtomId();
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
		assertNull(assignment.assign(1, MBT));
		assertNotNull(assignment.assign(1, FALSE));
	}

	@Test
	public void initializeDecisionLevelState() throws Exception {
		assignment.assign(1, MBT);
		assignment.choose(2, MBT);
		assignment.choose(1, TRUE);
	}

	@Test
	public void checkToString() {
		assignment.assign(1, FALSE);
		assertEquals("[F_a(0)@0]", assignment.toString());

		assignment.assign(2, TRUE);
		assertEquals("[F_a(0)@0, T_a(1)@0]", assignment.toString());
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

		assignment.choose(1, TRUE);

		assertEquals(TRUE, assignment.getTruth(1));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Arrays.asList(3, 1)));
		assertEquals(0, assignment.getMBTCount());

		assignment.backtrack();

		assertEquals(MBT, assignment.getTruth(1));
		assertEquals(FALSE, assignment.getTruth(2));
		assertEquals(TRUE, assignment.getTruth(3));
		assertEquals(assignment.getTrueAssignments(), new HashSet<>(Collections.singletonList(3)));
		assertEquals(1, assignment.getMBTCount());

		assignment.choose(4, MBT);
		assignment.assign(5, MBT);

		assertEquals(MBT, assignment.getTruth(4));
		assertEquals(MBT, assignment.getTruth(5));

		assignment.backtrack();

		assertFalse(assignment.isAssigned(4));
		assertFalse(assignment.isAssigned(5));

		assignment.choose(4, TRUE);

		assertEquals(TRUE, assignment.getTruth(4));

		assignment.backtrack();

		assertNull(assignment.getTruth(4));
	}

	@Test
	public void assignmentsToProcess() throws Exception {
		assignment.assign(1, MBT);

		Assignment.Pollable queue = assignment.getAssignmentsToProcess();
		assertEquals(1, queue.remove());

		assignment.choose(2, MBT);
		assignment.choose(1, TRUE);

		assertEquals(2, queue.remove());
		assertEquals(1, queue.peek());

		queue = assignment.getAssignmentsToProcess();
		assertEquals(1, queue.remove());
	}

	@Test
	public void newAssignmentsIteratorAndBacktracking() throws Exception {
		Iterator<Integer> newAssignmentsIterator;

		assignment.assign(1, MBT);
		assignment.choose(2, MBT);

		newAssignmentsIterator = assignment.getNewPositiveAssignmentsIterator();

		assertEquals(1, (int)newAssignmentsIterator.next());
		assertEquals(2, (int)newAssignmentsIterator.next());
		assertFalse(newAssignmentsIterator.hasNext());


		assignment.choose(1, TRUE);
		assignment.backtrack();
		assignment.assign(3, FALSE);

		newAssignmentsIterator = assignment.getNewPositiveAssignmentsIterator();
		assertEquals(3, (int)newAssignmentsIterator.next());
		assertFalse(newAssignmentsIterator.hasNext());
	}

	@Test
	public void newAssignmentsIteratorLowerDecisionLevelAndBacktracking() throws Exception {
		Iterator<Integer> newAssignmentsIterator;

		assignment.choose(1, MBT);
		assignment.choose(2, MBT);
		assignment.assign(3, MBT, null, 1);
		assignment.backtrack();

		newAssignmentsIterator = assignment.getNewPositiveAssignmentsIterator();
		assertEquals(1, (int)newAssignmentsIterator.next());
		assertEquals(3, (int)newAssignmentsIterator.next());
		assertFalse(newAssignmentsIterator.hasNext());
	}

	@Test
	public void iteratorAndBacktracking() throws Exception {
		Assignment.Pollable assignmentsToProcess = assignment.getAssignmentsToProcess();

		assignment.assign(1, MBT);
		assertEquals(1, assignmentsToProcess.remove());

		assignment.choose(2, MBT);
		assertEquals(2, assignmentsToProcess.remove());

		assignment.choose(1, TRUE);
		assertEquals(1, assignmentsToProcess.remove());

		assignment.backtrack();

		assignment.assign(3, FALSE);
		assertEquals(3, assignmentsToProcess.remove());
	}

	@Test
	public void mbtCounterAssignMbtToFalseOnLowerDecisionLevel() {
		assertNull(assignment.choose(1, TRUE));
		assertNull(assignment.choose(2, FALSE));

		assertNull(assignment.assign(3, MBT, null, 2));
		assertEquals(1, assignment.getMBTCount());

		assertNull(assignment.choose(4, TRUE));

		assertNotNull(assignment.assign(3, FALSE, null, 1));

		assignment.backtrack();
		assignment.backtrack();

		assertEquals(0, assignment.getMBTCount());
	}

	@Test
	public void numberOfAssignedAtoms() throws Exception {
		assignment.assign(1, MBT);
		assertEquals(1, assignment.getNumberOfAssignedAtoms());
		assignment.assign(2, FALSE);
		assertEquals(2, assignment.getNumberOfAssignedAtoms());
		assignment.assign(3, MBT);
		assignment.assign(3, TRUE);
		assertEquals(3, assignment.getNumberOfAssignedAtoms());
		assignment.choose(1, TRUE);
		assertEquals(3, assignment.getNumberOfAssignedAtoms());
		assertEquals(3, assignment.getNumberOfAtomsAssignedFromDecisionLevel(0));
		assertEquals(1, assignment.getNumberOfAtomsAssignedFromDecisionLevel(1));
	}
}