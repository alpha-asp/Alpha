package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
abstract class AbstractSolver implements Solver {
	protected final Grounder grounder;

	protected AbstractSolver(Grounder grounder) {
		this.grounder = grounder;
	}

	public abstract void computeAnswerSets(int numAnswerSetsRequested);
}
