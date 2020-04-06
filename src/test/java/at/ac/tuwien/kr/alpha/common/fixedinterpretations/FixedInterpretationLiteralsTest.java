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
			+ "negative_external_with_output :- Y = 2121, MO = 1, D = 1, H = 21, MI = 11, S = 12, "
			+ "not &stdlib_datetime_parse[\"22.02.2222 21:11:12\", \"dd.MM.yyyy HH:mm:ss\"](Y, MO, D, H, MI, S)."
			+ "negative_external_with_output_dontfire :- Y = 2222, MO = 2, D = 22, H = 21, MI = 11, S = 12,"
			+ "not &stdlib_datetime_parse[\"22.02.2222 21:11:12\", \"dd.MM.yyyy HH:mm:ss\"](Y, MO, D, H, MI, S).";

	private AnswerSet answerSet;

	public FixedInterpretationLiteralsTest() {
		Alpha alpha = new Alpha();
		Map<String, PredicateInterpretation> externals = new HashMap<>();
		externals.putAll(Externals.scan(AspStandardLibrary.class));
		externals.putAll(Externals.scan(FixedInterpretationLiteralsTest.class));
		Optional<AnswerSet> answer = alpha.solve(alpha.readProgramString(TEST_PROG, externals)).findFirst();
		if (!answer.isPresent()) {
			throw new IllegalStateException("Test setup failed, no answer set!");
		}
		this.answerSet = answer.get();
	}

	@Test
	public void positiveNumericComparison() {
		Assert.assertTrue(this.answerSet.getPredicates().contains(Predicate.getInstance("positive_numeric_comparison", 0)));
	}

	@Test
	public void negativeNumericComparison() {
		Assert.assertTrue(this.answerSet.getPredicates().contains(Predicate.getInstance("negative_numeric_comparison", 0)));
	}

	@Test
	public void positiveUnaryExternal() {
		Assert.assertTrue(this.answerSet.getPredicates().contains(Predicate.getInstance("positive_unary_external", 0)));
	}

	@Test
	public void negativeUnaryExternal() {
		Assert.assertTrue(this.answerSet.getPredicates().contains(Predicate.getInstance("negative_unary_external", 0)));
	}

	@Test
	public void positiveExternalWithOutput() {
		Assert.assertTrue(this.answerSet.getPredicates().contains(Predicate.getInstance("positive_external_with_output", 0)));
	}

	@Test
	public void negativeExternalWithOutput() {
		Assert.assertTrue(this.answerSet.getPredicates().contains(Predicate.getInstance("negative_external_with_output", 0)));
	}

	@Test
	public void negativeExternalWithOutputDontfire() {
		Assert.assertFalse(this.answerSet.getPredicates().contains(Predicate.getInstance("negative_external_with_output_dontfire", 0)));
	}

}
