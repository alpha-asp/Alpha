package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.CoreAnswerSet;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;

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

	protected CoreAnswerSet translate(Iterable<Integer> assignment) {
		return grounder.assignmentToAnswerSet(assignment);
	}

	protected abstract boolean tryAdvance(Consumer<? super CoreAnswerSet> action);

	@Override
	public Spliterator<CoreAnswerSet> spliterator() {
		return new Spliterators.AbstractSpliterator<CoreAnswerSet>(Long.MAX_VALUE, 0) {
			@Override
			public boolean tryAdvance(Consumer<? super CoreAnswerSet> action) {
				return AbstractSolver.this.tryAdvance(action);
			}
		};
	}
}
