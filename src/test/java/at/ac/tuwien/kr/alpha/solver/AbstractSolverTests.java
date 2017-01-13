package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

@RunWith(Parameterized.class)
public abstract class AbstractSolverTests {
	@Parameters
	public static Iterable<? extends Function<Grounder, Solver>> factories() {
		return Arrays.asList(g -> {
			return new NaiveSolver(g);
		}, g -> {
			return new DefaultSolver(g, new Random());
		}, g -> {
			return new DefaultSolver(g, new Random(0));
		});
	}

	@Parameter
	public Function<Grounder, Solver> factory;

	protected Solver getInstance(Grounder g) {
		return factory.apply(g);
	}
}
