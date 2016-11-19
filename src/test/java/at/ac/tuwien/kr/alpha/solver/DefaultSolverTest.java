package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

public class DefaultSolverTest extends AbstractSolverTest {
	@Override
	protected Solver getInstance(Grounder grounder) {
		return new DefaultSolver(grounder);
	}
}
