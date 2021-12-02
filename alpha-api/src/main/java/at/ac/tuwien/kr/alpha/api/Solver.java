package at.ac.tuwien.kr.alpha.api;

import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * API representation of a solving component for a specific ASP program.
 * Exposes {@link AnswerSet}s of the program for which the solver was created through {@link Solver#stream()}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Solver {

	Spliterator<AnswerSet> spliterator();

	default Stream<AnswerSet> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Rather than streaming {@link AnswerSet}s, collect the whole stream into a {@link Set}. Note that this method blocks unitl all answer sets
	 * are calculated, which may take a long time.
	 */
	default Set<AnswerSet> collectSet() {
		return stream().collect(Collectors.toSet());
	}

	/**
	 * Rather than streaming {@link AnswerSet}s, collect the whole stream into a {@link List}. Note that this method blocks unitl all answer
	 * sets
	 * are calculated, which may take a long time.
	 */
	default List<AnswerSet> collectList() {
		return stream().collect(Collectors.toList());
	}

}
