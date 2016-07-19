package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;

import java.util.function.Predicate;

public final class SolverFactory {
	public static Solver getInstance(String name, Grounder grounder, Predicate<GrounderPredicate> filter) {
		switch (name.toLowerCase()) {
			case "dummy": return new DummySolver(grounder);
			case "leutgeb": return new LeutgebSolver(grounder, filter);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}
}
