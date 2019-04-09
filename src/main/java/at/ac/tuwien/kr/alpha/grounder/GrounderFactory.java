package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;

public final class GrounderFactory {
	public static Grounder getInstance(String name, Program program, AtomStore atomStore, HeuristicsConfiguration heuristicsConfiguration, java.util.function.Predicate<Predicate> filter, boolean useCountingGridNormalization, Bridge... bridges) {
		switch (name.toLowerCase()) {
			case "naive":
				return new NaiveGrounder(program, atomStore, heuristicsConfiguration, filter, useCountingGridNormalization, bridges);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}

	public static Grounder getInstance(String name, Program program, AtomStore atomStore, HeuristicsConfiguration heuristicsConfiguration) {
		return getInstance(name, program, atomStore, heuristicsConfiguration, p -> true, false);
	}
}
