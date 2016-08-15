package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

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

	protected abstract boolean tryAdvance(Consumer<? super AnswerSet> action);

	@Override
	public Spliterator<AnswerSet> spliterator() {
		return new Spliterators.AbstractSpliterator<AnswerSet>(Long.MAX_VALUE, 0) {
			@Override
			public boolean tryAdvance(Consumer<? super AnswerSet> action) {
				return AbstractSolver.this.tryAdvance(action);
			}
		};
	}
}
