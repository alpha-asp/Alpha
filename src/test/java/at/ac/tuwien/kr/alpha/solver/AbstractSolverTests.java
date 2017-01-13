package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

@RunWith(Parameterized.class)
public abstract class AbstractSolverTests {
	@Parameters(name = "{0}")
	public static Collection<Object[]> factories() {
		return Arrays.asList(new Object[][]{
			{
				"NaiveSolver",
				(Function<Grounder, Solver>) NaiveSolver::new
			},
			{
				"DefaultSolver (random)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random());
				}
			},
			{
				"Defaultsolver (deterministic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0));
				}
			}
		});
	}

	@Parameter(value = 0)
	public String solverName;

	@Parameter(value = 1)
	public Function<Grounder, Solver> factory;

	protected Solver getInstance(Grounder g) {
		return factory.apply(g);
	}
}
