package at.ac.tuwien.kr.alpha.common.fixedinterpretations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.externals.Externals;
import at.ac.tuwien.kr.alpha.api.externals.stdlib.AspStandardLibrary;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;

public class FixedInterpretationLiteralsTest {

	@at.ac.tuwien.kr.alpha.api.externals.Predicate
	public static boolean isOne(int value) {
		return value == 1;
	}

	private static final String TEST_PROG = "positive_numeric_comparison :- 2 < 3."
			+ "negative_numeric_comparison :- not 3 < 2."
			+ "positive_unary_external :- &isOne[1]."
			+ "negative_unary_external :- not &isOne[2]."
			+ "positive_external_with_output :- "
			+ "&stdlib_datetime_parse[\"01.01.2121 21:11:12\", \"dd.MM.yyyy HH:mm:ss\"](Y, MO, D, H, MI, S), Y = 2121, MO = 1, D = 1, H = 21, MI = 11, S = 12."
			+ "positive_external_with_output_dontfire :- "
			+ "&stdlib_datetime_parse[\"01.01.2121 21:11:12\", \"dd.MM.yyyy HH:mm:ss\"](Y, MO, D, H, MI, S), Y = 2345, MO = 1, D = 1, H = 21, MI = 11, S = 12."
			+ "negative_external_with_output :- Y = 2121, MO = 1, D = 1, H = 21, MI = 11, S = 12, "
			+ "not &stdlib_datetime_parse[\"22.02.2222 21:11:12\", \"dd.MM.yyyy HH:mm:ss\"](Y, MO, D, H, MI, S)."
			+ "negative_external_with_output_dontfire :- Y = 2222, MO = 2, D = 22, H = 21, MI = 11, S = 12,"
			+ "not &stdlib_datetime_parse[\"22.02.2222 21:11:12\", \"dd.MM.yyyy HH:mm:ss\"](Y, MO, D, H, MI, S).";

	private Alpha alpha;
	private Map<String, PredicateInterpretation> externals;

	public FixedInterpretationLiteralsTest() {
		this.alpha = new Alpha();
		this.externals = new HashMap<>();
		this.externals.putAll(Externals.scan(AspStandardLibrary.class));
		this.externals.putAll(Externals.scan(FixedInterpretationLiteralsTest.class));
	}

	@Test
	public void positiveNumericComparison() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertTrue(answerSet.getPredicates().contains(Predicate.getInstance("positive_numeric_comparison", 0)));
	}

	@Test
	public void negativeNumericComparison() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertTrue(answerSet.getPredicates().contains(Predicate.getInstance("negative_numeric_comparison", 0)));
	}

	@Test
	public void positiveUnaryExternal() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertTrue(answerSet.getPredicates().contains(Predicate.getInstance("positive_unary_external", 0)));
	}

	@Test
	public void negativeUnaryExternal() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertTrue(answerSet.getPredicates().contains(Predicate.getInstance("negative_unary_external", 0)));
	}

	@Test
	public void positiveExternalWithOutput() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertTrue(answerSet.getPredicates().contains(Predicate.getInstance("positive_external_with_output", 0)));
	}

	@Test
	public void positiveExternalWithOutputDontfire() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertFalse(answerSet.getPredicates().contains(Predicate.getInstance("positive_external_with_output_dontfire", 0)));
	}	
	
	@Test
	public void negativeExternalWithOutput() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertTrue(answerSet.getPredicates().contains(Predicate.getInstance("negative_external_with_output", 0)));
	}

	@Test
	public void negativeExternalWithOutputDontfire() {
		Optional<AnswerSet> answer = this.alpha.solve(this.alpha.readProgramString(TEST_PROG, this.externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		AnswerSet answerSet = answer.get();
		Assert.assertFalse(answerSet.getPredicates().contains(Predicate.getInstance("negative_external_with_output_dontfire", 0)));
	}

}