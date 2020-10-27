package at.ac.tuwien.kr.alpha.common;

@FunctionalInterface
public interface AnswerSetFormatter<T> {
	T format(AnswerSet answerSet);
}
