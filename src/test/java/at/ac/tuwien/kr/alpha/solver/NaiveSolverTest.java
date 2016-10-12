package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

public class NaiveSolverTest extends AbstractSolverTest {
	@Override
	protected Solver getInstance(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		return new NaiveSolver(grounder, filter);
	}
}