package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.function.Consumer;

public class LeutgebSolver extends AbstractSolver {
	private final BasicAssignment assignment;
	private final BasicNoGoodStore store;
	private int decisionLevel;

	LeutgebSolver(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		super(grounder, filter);
		assignment = new BasicAssignment();
		store = new BasicNoGoodStore(assignment);
		decisionLevel = 0;
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		store.setDecisionLevel(decisionLevel);
		store.propagate();
		return false;
	}
}
