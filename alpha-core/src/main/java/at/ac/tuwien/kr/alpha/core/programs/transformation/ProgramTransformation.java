package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.core.programs.AbstractProgram;

/**
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public abstract class ProgramTransformation<I extends AbstractProgram<?>, O extends AbstractProgram<?>> {

	public abstract O apply(I inputProgram);

}
