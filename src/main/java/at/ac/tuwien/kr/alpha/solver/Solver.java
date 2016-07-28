package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;

import java.util.Spliterator;

@FunctionalInterface
public interface Solver {
	Spliterator<AnswerSet> spliterator();
}
