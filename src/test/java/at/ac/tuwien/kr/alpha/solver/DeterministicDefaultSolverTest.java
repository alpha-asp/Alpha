package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Random;

public class DeterministicDefaultSolverTest extends AbstractSolverTest {
	@Override
	protected Solver getInstance(Grounder grounder) {
		return new DefaultSolver(grounder, new Random(0));
	}
}
