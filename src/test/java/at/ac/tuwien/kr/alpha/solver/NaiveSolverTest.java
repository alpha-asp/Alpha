package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

public class NaiveSolverTest extends AbstractSolverTest {
	@Override
	protected Solver getInstance(Grounder grounder) {
		return new NaiveSolver(grounder);
	}
}