package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
abstract class AbstractSolver implements Solver {
	protected final Grounder grounder;
	protected final AtomStore atomStore;

	protected AbstractSolver(AtomStore atomStore, Grounder grounder) {
		this.atomStore = atomStore;
		this.grounder = grounder;
	}

	protected AnswerSet translate(Iterable<Integer> assignment) {
		return grounder.assignmentToAnswerSet(assignment);
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
