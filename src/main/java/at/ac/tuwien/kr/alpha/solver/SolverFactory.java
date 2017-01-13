package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Random;

public final class SolverFactory {
	public static Solver getInstance(String name, Grounder grounder, Random random) {
		switch (name.toLowerCase()) {
			case "naive" : return new NaiveSolver(grounder);
			case "default": return new DefaultSolver(grounder, random);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}
}
