package at.ac.tuwien.kr.alpha.core.solver;

import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import at.ac.tuwien.kr.alpha.core.common.CoreAnswerSet;

@FunctionalInterface
public interface Solver {
	Spliterator<CoreAnswerSet> spliterator();

	default Stream<CoreAnswerSet> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	default Set<CoreAnswerSet> collectSet() {
		return stream().collect(Collectors.toSet());
	}

	default List<CoreAnswerSet> collectList() {
		return stream().collect(Collectors.toList());
	}
}
