package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.Program;
import at.ac.tuwien.kr.alpha.api.rules.Rule;

/**
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public abstract class ProgramTransformer<I extends Program<? extends Rule<?>>, O extends Program<? extends Rule<?>>> {

	public abstract O transform(I inputProgram);

}
