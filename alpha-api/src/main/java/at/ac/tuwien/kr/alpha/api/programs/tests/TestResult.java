package at.ac.tuwien.kr.alpha.api.programs.tests;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestResult {

	List<TestCaseResult> getTestCaseResults();

	default boolean isSuccess() {
		return getTestCaseResults().stream().allMatch(TestCaseResult::isSuccess);
	}

	interface TestCaseResult {

		String getTestCaseName();

		Optional<String> answerSetCountVerificationResult();

		int getAssertionsPassed();

		int getAssertionsFailed();

		Map<Assertion, List<String>> getAssertionErrors();

		default boolean isSuccess() {
			return answerSetCountVerificationResult().isEmpty() && getAssertionsFailed() == 0;
		}

	}
}
