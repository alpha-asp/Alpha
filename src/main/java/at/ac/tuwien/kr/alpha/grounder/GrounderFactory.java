package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

public final class GrounderFactory {
	public static Grounder getInstance(String name, ParsedProgram program, java.util.function.Predicate<Predicate> filter) {
		switch (name.toLowerCase()) {
			case "naive": return new NaiveGrounder(program, filter);
			case "alphahex": return new AlphahexGrounder(program, filter);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}
}
