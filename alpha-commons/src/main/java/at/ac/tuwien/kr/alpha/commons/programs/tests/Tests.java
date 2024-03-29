package at.ac.tuwien.kr.alpha.commons.programs.tests;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntPredicate;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestResult;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;

public final class Tests {

	private Tests() {
		throw new AssertionError("Cannot instantiate utility class!");
	}
	
	public static TestCase newTestCase(final String name, final IntPredicate answerSetCountVerifier, final Set<BasicAtom> input, final List<Assertion> assertions) {
		return new TestCaseImpl(name, answerSetCountVerifier, input, assertions);
	}

	public static Assertion newAssertion(final Assertion.Mode mode, final ASPCore2Program verifier) {
		return new AssertionImpl(mode, verifier);
	}

	public static IntPredicate newIsUnsatCondition() {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return value == 0;
			}

			@Override
			public String toString() {
				return "unsat";
			}

		};
	}

	public static IntPredicate newAnswerSetCountCondition(ComparisonOperator cmpOp, int number) {
		return new IntPredicate() {
			@Override
			public boolean test(int value) {
				return cmpOp.compare(Terms.newConstant(value), Terms.newConstant(number));
			}

			@Override
			public String toString() {
				return cmpOp.toString() + " " + Integer.toString(number);
			}

		};
	}

	public static TestResult.TestCaseResult newTestCaseResult(String testCaseName, Optional<String> answerSetCountResult, int assertionsPassed, int assertionsFailed, Map<Assertion, List<String>> assertionErrors) {
		return  new TestResult.TestCaseResult() {
			@Override
			public String getTestCaseName() {
				return testCaseName;
			}

			@Override
			public Optional<String> answerSetCountVerificationResult() {
				return answerSetCountResult;
			}

			@Override
			public int getAssertionsPassed() {
				return assertionsPassed;
			}

			@Override
			public int getAssertionsFailed() {
				return assertionsFailed;
			}

			@Override
			public Map<Assertion, List<String>> getAssertionErrors() {
				return assertionErrors;
			}
		};
	}

}
