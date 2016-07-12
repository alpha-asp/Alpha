package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class IdentityProgramTransformation extends ProgramTransformationBase {
	@Override
	public ParsedProgram transform(ParsedProgram inputProgram) {
		return inputProgram;
	}


}
