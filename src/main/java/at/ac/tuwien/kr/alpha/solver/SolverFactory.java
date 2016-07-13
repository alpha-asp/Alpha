package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

public final class SolverFactory {
	public static Solver getInstance(String name, Grounder grounder) {
		switch (name.toLowerCase()) {
			case "dummy": return new DummySolver(grounder);
			case "leutgeb": return new LeutgebSolver(grounder);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}
}
