package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;

public final class GrounderFactory {
	public static Grounder getInstance(String name, InternalProgram programAnalysis, AtomStore atomStore, java.util.function.Predicate<Predicate> filter,
			boolean useCountingGridNormalization, Bridge... bridges) {
		switch (name.toLowerCase()) {
			case "naive":
				return new NaiveGrounder(programAnalysis, atomStore, filter, useCountingGridNormalization, bridges);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}

	public static Grounder getInstance(String name, InternalProgram programAnalysis, AtomStore atomStore) {
		return getInstance(name, programAnalysis, atomStore, p -> true, false);
	}
}
