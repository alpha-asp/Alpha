package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;

import java.util.function.Predicate;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummySolver extends AbstractSolver {
	public DummySolver(Grounder grounder) {
		super(grounder, p -> true);
	}

	@Override
	public AnswerSet get() {
		return null;
	}
}
