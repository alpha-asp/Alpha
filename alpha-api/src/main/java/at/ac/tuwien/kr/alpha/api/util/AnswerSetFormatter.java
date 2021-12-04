package at.ac.tuwien.kr.alpha.api.util;

import at.ac.tuwien.kr.alpha.api.AnswerSet;

/**
 * Formats an {@link AnswerSet} to an instance of type T.
 * 
 * @param <T> the type to which to convert the answer set.
 *            Copyright (c) 2021, the Alpha Team.
 */
@FunctionalInterface
public interface AnswerSetFormatter<T> {
	T format(AnswerSet answerSet);
}
