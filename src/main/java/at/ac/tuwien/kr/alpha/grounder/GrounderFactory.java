package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AtomTranslator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;

public final class GrounderFactory {
	public static Grounder getInstance(String name, Program program, AtomTranslator atomTranslator, java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		switch (name.toLowerCase()) {
			case "naive":
				return new NaiveGrounder(program, atomTranslator, filter, bridges);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}

	public static Grounder getInstance(String name, Program program, AtomTranslator atomTranslator) {
		return getInstance(name, program, atomTranslator, p -> true);
	}
}
