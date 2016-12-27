package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
@FunctionalInterface
public interface Transformation {
	ParsedProgram transform(ParsedProgram inputProgram);
}
