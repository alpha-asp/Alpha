package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.AbstractGrounder;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class AbstractSolver {

	private AbstractGrounder grounder;

	public AbstractSolver(AbstractGrounder grounder) {
		this.grounder = grounder;
	}
}
