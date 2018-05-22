package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;

public final class GrounderFactory {
	public static Grounder getInstance(String name, Program program, java.util.function.Predicate<Predicate> filter, boolean useCountingGridNormalization, Bridge... bridges) {
		switch (name.toLowerCase()) {
			case "naive":
				return new NaiveGrounder(program, filter, useCountingGridNormalization, bridges);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}

	public static Grounder getInstance(String name, Program program) {
		return getInstance(name, program, p -> true, false);
	}
}
