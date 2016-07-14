package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.AnswerSet;

import java.util.function.Supplier;

@FunctionalInterface
public interface Solver extends Supplier<AnswerSet> {
}
