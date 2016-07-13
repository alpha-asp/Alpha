package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.AbstractGrounder;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class AbstractSolver {

	protected AbstractGrounder grounder;

	public AbstractSolver(AbstractGrounder grounder) {
		this.grounder = grounder;
	}

	public abstract void computeAnswerSets(int numAnswerSetsRequested);
}
