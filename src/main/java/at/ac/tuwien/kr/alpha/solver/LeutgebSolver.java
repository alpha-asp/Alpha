package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Iterator;
import java.util.function.Consumer;

public class LeutgebSolver extends AbstractSolver {
	LeutgebSolver(Grounder grounder) {
		super(grounder);
	}

	@Override
	public AnswerSet get() {
		throw new UnsupportedOperationException();
	}
}
