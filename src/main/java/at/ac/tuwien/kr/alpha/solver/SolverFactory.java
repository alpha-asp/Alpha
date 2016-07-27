package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.common.Predicate;

public final class SolverFactory {
	public static Solver getInstance(String name, Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		switch (name.toLowerCase()) {
			case "dummy": return new DummySolver(grounder);
			case "leutgeb": return new LeutgebSolver(grounder, filter);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}
}
