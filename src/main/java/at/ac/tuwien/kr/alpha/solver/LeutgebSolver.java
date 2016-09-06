package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.function.Consumer;

public class LeutgebSolver extends AbstractSolver {
	private final Assignment<ThriceTruth> assignment;
	private final NoGoodStore store;
	private int decisionLevel;

	LeutgebSolver(Grounder grounder, java.util.function.Predicate<Predicate> filter) {
		super(grounder, filter);
		assignment = new Assignment<>();
		store = new NoGoodStore(assignment);
		decisionLevel = 0;
	}

	@Override
	protected boolean tryAdvance(Consumer<? super AnswerSet> action) {
		grounder.getNoGoods().values().parallelStream().forEach(store::add);
		store.propagate(decisionLevel);
		return false;
	}
}
