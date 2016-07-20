package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.assertNotNull;

public class LeutgebSolverTest {
	private static final Predicate<GrounderPredicate> alwaysTrue = x -> true;
	private Grounder grounder;

	@Before
	public void instantiateGrounder() {
		grounder = new DummyGrounder();
	}

	@Test
	public void returnsAnswerSet() {
		final Solver solver = new LeutgebSolver(grounder, alwaysTrue);
		assertNotNull(solver.get());
	}
}
