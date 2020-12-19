package at.ac.tuwien.kr.alpha.core.common;

@FunctionalInterface
public interface AnswerSetFormatter<T> {
	T format(CoreAnswerSet answerSet);
}
