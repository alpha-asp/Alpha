package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class AbstractGrounder implements Grounder {
	protected ParsedProgram program;
	protected final java.util.function.Predicate<Predicate> filter;

	protected AbstractGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter) {
		this.program = program;
		this.filter = filter;
	}

	protected AbstractGrounder(ParsedProgram program) {
		this(program, p -> true);
	}
}
