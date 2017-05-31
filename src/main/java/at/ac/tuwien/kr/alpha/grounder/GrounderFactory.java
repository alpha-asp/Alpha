package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

public final class GrounderFactory {
	public static Grounder getInstance(String name, ParsedProgram program, java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		switch (name.toLowerCase()) {
			case "naive": return new NaiveGrounder(program, filter, bridges);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}
}
