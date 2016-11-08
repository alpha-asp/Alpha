package at.ac.tuwien.kr.alpha.solver;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;
import static org.junit.Assert.*;

public class BasicAssignmentTest {
	private final BasicAssignment assignment;

	public BasicAssignmentTest() {
		assignment = new BasicAssignment(null);
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
		assertTrue(assignment.containsRelaxed(+1));
		assertFalse(assignment.containsRelaxed(-1));

		assignment.assign(2, FALSE);
		assertTrue(assignment.containsRelaxed(-2));
		assertFalse(assignment.containsRelaxed(+2));

		assignment.assign(1, MBT);
		assertTrue(assignment.containsRelaxed(+1));
		assertFalse(assignment.containsRelaxed(-1));
	}

	@Test
	public void iterator() throws Exception {
		assignment.assign(1, MBT);

		Iterator<Map.Entry<Integer, Assignment.Entry>> it = assignment.iterator();
		assertEquals((int)it.next().getKey(), 1);

		assignment.guess(2, MBT);
		assignment.guess(1, TRUE);

		assertEquals((int)it.next().getKey(), 2);
		assertEquals((int)it.next().getKey(), 1);

		it = assignment.iterator();
		assertEquals((int)it.next().getKey(), 1);
		assertEquals((int)it.next().getKey(), 2);
		assertEquals((int)it.next().getKey(), 1);

		int count = 0;

		it = assignment.iterator();
		while (it.hasNext()) {
			switch (count) {
				case 0:
					assertEquals((int) it.next().getKey(), 1);
					break;
				case 1:
					assertEquals((int) it.next().getKey(), 2);
					break;
				case 2:
					assertEquals((int) it.next().getKey(), 1);
					break;
			}
			count++;
		}

		assertEquals(3, count);
	}

	@Test
	public void iteratorAndBacktracking() throws Exception {
		Iterator<Map.Entry<Integer, Assignment.Entry>> it = assignment.iterator();

		assignment.assign(1, MBT);
		assertEquals((int)it.next().getKey(), 1);

		assignment.guess(2, MBT);
		assertEquals((int)it.next().getKey(), 2);

		assignment.guess(1, TRUE);
		assertEquals((int)it.next().getKey(), 1);

		assignment.backtrack();

		assignment.assign(3, FALSE);
		assertEquals((int)it.next().getKey(), 3);
	}
}