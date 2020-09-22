package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.program.AbstractProgram;

/**
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public abstract class ProgramTransformation<I extends AbstractProgram<?>, O extends AbstractProgram<?>> {

	public abstract O apply(I inputProgram);

	/**
	 * Chains an additional program transformation to this one.
	 * 
	 * @param <O1>
	 * @param nextTransform
	 * @return
	 */
	public <P extends AbstractProgram<?>> ProgramTransformation<I, P> andThen(ProgramTransformation<O, P> nextTransform) {
		return new ProgramTransformation<I, P>() {
			@Override
			public P apply(I input) {
				O intermediateResult = ProgramTransformation.this.apply(input);
				return nextTransform.apply(intermediateResult);
			}
		};
	}

}
