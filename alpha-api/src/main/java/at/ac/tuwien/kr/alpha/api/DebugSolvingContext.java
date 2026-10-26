package at.ac.tuwien.kr.alpha.api;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;

/**
 * Wrapper object for debug information on program preprocessing produced by {@link Alpha}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface DebugSolvingContext {

	/**
	 * The normalized version of the {@link InputProgram} that is being solved.
	 * See {@link Alpha#normalizeProgram(InputProgram)}.
	 */
	NormalProgram getNormalizedProgram();

	/**
	 * The fully preprocessed version of the {@link InputProgram} that is being solved.
	 * This differs from the value of {@link DebugSolvingContext#getNormalizedProgram()} in the stratified part of the normalized program may
	 * already be evaluated depending on the respective configuration of {@link Alpha}.
	 */
	NormalProgram getPreprocessedProgram();

	/**
	 * The {@link DependencyGraph} of the program being solved.
	 */
	DependencyGraph getDependencyGraph();

	/**
	 * The {@link ComponentGraph} of the program being solved.
	 */
	ComponentGraph getComponentGraph();

	/**
	 * A {@link Solver} instance pre-loaded with the program being solved, from which {@link AnswerSet}s can be streamed.
	 */
	Solver getSolver();

}
