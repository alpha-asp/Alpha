package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Program;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class IdentityProgramTransformation extends ProgramTransformationBase {
	@Override
	public Program transform(Program inputProgram) {
		return inputProgram;
	}


}
