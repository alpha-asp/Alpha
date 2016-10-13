package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

public final class SolverFactory {
	public static Solver getInstance(String name, Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		switch (name.toLowerCase()) {
			case "naive" : return new NaiveSolver(grounder, filter);
			case "default": return new DefaultSolver(grounder, filter);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}
}
