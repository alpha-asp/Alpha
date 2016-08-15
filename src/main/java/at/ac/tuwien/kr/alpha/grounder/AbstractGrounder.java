package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class AbstractGrounder implements Grounder {
	protected ParsedProgram program;

	protected AbstractGrounder(ParsedProgram program) {
		this.program = program;
	}
}
