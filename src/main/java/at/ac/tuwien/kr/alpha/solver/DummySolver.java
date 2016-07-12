package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummySolver extends AbstractSolver {
	public DummySolver(Grounder grounder) {
		super(grounder);
	}

	@Override
	public AnswerSet get() {
		return null;
	}
}
