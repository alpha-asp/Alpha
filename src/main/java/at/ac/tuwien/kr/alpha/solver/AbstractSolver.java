package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.Predicate;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
abstract class AbstractSolver implements Solver {
	protected final Grounder grounder;
	protected final java.util.function.Predicate<Predicate> filter;

	protected AbstractSolver(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		this.grounder = grounder;
		this.filter = filter;
	}

	protected AnswerSet translate(int[] assignment) {
		return grounder.assignmentToAnswerSet(filter, assignment);
	}
}
