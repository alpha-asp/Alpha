package at.ac.tuwien.kr.alpha.solver;

import org.junit.Test;

public class BasicAssignmentTest {
	@Test(expected = IllegalArgumentException.class)
	public void assign() throws Exception {
		final BasicAssignment assignment = new BasicAssignment();
		assignment.assign(0, null, 0);
	}
}