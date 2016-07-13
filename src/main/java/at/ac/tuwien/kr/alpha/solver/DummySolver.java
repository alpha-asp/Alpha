package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.grounder.AbstractGrounder;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummySolver extends AbstractSolver {
	@Override
	public void computeAnswerSets(int numAnswerSetsRequested) {

	}

	public DummySolver(AbstractGrounder grounder) {
		super(grounder);
	}
}
