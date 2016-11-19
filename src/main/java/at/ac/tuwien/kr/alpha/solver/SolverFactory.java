package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

public final class SolverFactory {
	public static Solver getInstance(String name, Grounder grounder) {
		switch (name.toLowerCase()) {
			case "naive" : return new NaiveSolver(grounder);
			case "default": return new DefaultSolver(grounder);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}
}
