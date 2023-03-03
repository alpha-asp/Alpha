package at.ac.tuwien.kr.alpha.api.programs.tests;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestResult {

	List<TestCaseResult> getTestCaseResults();

	interface TestCaseResult {

		String getTestCaseName();

		Optional<String> answerSetCountVerificationResult();

		int getAssertionsPassed();

		int getAssertionsFailed();

		int getAssertionsSkipped();

		Map<Assertion, List<String>> getAssertionErrors();

		default boolean isSuccessful() {
			return answerSetCountVerificationResult().isEmpty() && getAssertionsFailed() == 0 && getAssertionsSkipped() == 0;
		}

	}
}
