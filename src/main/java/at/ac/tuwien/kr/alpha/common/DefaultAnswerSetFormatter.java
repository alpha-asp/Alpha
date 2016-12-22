package at.ac.tuwien.kr.alpha.common;

public class DefaultAnswerSetFormatter implements AnswerSetFormatter<String> {
	@Override
	public String format(AnswerSet answerSet) {
		return answerSet.toString();
	}
}
