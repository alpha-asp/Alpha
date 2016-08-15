package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

public final class GrounderFactory {
	public static Grounder getInstance(String name, ParsedProgram program) {
		switch (name.toLowerCase()) {
			case "rete": return new ReteGrounder(program);
		}
		throw new IllegalArgumentException("Unknwon grounder requested.");
	}
}
