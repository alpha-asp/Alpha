package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.common.Predicate;

public class LeutgebSolver extends AbstractSolver {
	LeutgebSolver(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		super(grounder, filter);
	}

	@Override
	public AnswerSet get() {
		throw new UnsupportedOperationException();
	}
}
