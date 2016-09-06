package at.ac.tuwien.kr.alpha.solver;

import org.junit.Test;

public class AssignmentTest {
	@Test(expected = IllegalArgumentException.class)
	public void assign() throws Exception {
		final Assignment<ThriceTruth> assignment = new Assignment<>();
		assignment.assign(0, null, 0);
	}
}