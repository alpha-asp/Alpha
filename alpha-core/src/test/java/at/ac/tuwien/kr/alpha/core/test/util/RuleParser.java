package at.ac.tuwien.kr.alpha.core.test.util;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.core.parser.evolog.EvologProgramParser;

public class RuleParser {

	public static Rule<Head> parse(String str) {
		ProgramParser parser = new EvologProgramParser();
		InputProgram prog = parser.parse(str);
		if (!prog.getFacts().isEmpty()) {
			throw new IllegalArgumentException("Excepted exactly one rule and no facts!");
		}
		if (prog.getRules().size() != 1) {
			throw new IllegalArgumentException("Excepted exactly one rule");
		}
		return prog.getRules().get(0);
	}

}
