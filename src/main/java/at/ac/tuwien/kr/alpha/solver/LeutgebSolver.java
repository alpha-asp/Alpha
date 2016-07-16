package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;

import java.util.function.Predicate;

public class LeutgebSolver extends AbstractSolver {
	LeutgebSolver(Grounder grounder, Predicate<GrounderPredicate> filter) {
		super(grounder, filter);
	}

	@Override
	public AnswerSet get() {
		throw new UnsupportedOperationException();
	}
}
