package at.ac.tuwien.kr.alpha.api.test.result.impl;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.mapper.AnswerSetToObjectMapper;
import at.ac.tuwien.kr.alpha.common.AnswerSet;

public class AnswerSetToAssertionErrorsMapper implements AnswerSetToObjectMapper<List<AssertionError>> {

	@Override
	public List<AssertionError> mapFromAnswerSet(AnswerSet answerSet) {
		List<AssertionError> retVal = new ArrayList<>();
		// TODO
		return retVal;
	}

}
