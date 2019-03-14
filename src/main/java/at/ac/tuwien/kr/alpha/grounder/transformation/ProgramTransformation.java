package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.program.Program;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
@FunctionalInterface
public interface ProgramTransformation {
	void transform(Program inputProgram);
}
