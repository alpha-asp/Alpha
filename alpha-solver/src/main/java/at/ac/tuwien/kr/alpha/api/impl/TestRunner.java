package at.ac.tuwien.kr.alpha.api.impl;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestResult;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.tests.Tests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

class TestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestRunner.class);

	private final Alpha alpha;

	TestRunner(final Alpha alpha) {
		this.alpha = alpha;
	}

	TestResult test(ASPCore2Program program) {
		LOGGER.info("Running unit tests..");
		NormalProgram programUnderTest = alpha.normalizeProgram(program);
		List<TestResult.TestCaseResult> testCaseResults = program.getTestCases().stream()
				.map((tc) -> runTestCase(programUnderTest, tc))
				.collect(Collectors.toList());
		return () -> testCaseResults;
	}

	private TestResult.TestCaseResult runTestCase(NormalProgram programUnderTest, TestCase testCase) {
		LOGGER.info("Running test case " + testCase.getName());
		List<Atom> facts = new ArrayList<>(programUnderTest.getFacts());
		facts.addAll(testCase.getInput());
		NormalProgram prog = Programs.newNormalProgram(programUnderTest.getRules(), facts, programUnderTest.getInlineDirectives());
		Set<AnswerSet> answerSets;
		try {
			answerSets = alpha.solve(prog).collect(Collectors.toSet());
		} catch (Throwable t) {
			LOGGER.error("Test execution failed due to internal error", t);
			return Tests.newTestCaseResult(
					testCase.getName(), Optional.of("Test execution failed due to internal error: " + t.getMessage()), 0, 0, Collections.emptyMap());
		}
		IntPredicate answerSetsVerifier = testCase.getAnswerSetCountVerifier();
		Optional<String> answerSetCountErrMsg;
		if (!answerSetsVerifier.test(answerSets.size())) {
			answerSetCountErrMsg = Optional.of("Answer Set count incorrect, verifier: " + answerSetsVerifier + " failed, count is " + answerSets.size());
		} else {
			answerSetCountErrMsg = Optional.empty();
		}
		int passedCnt = 0;
		int failedCnt = 0;
		Map<Assertion, List<String>> assertionErrors = new LinkedHashMap<>();
		for (Assertion assertion : testCase.getAssertions()) {
			List<String> errors;
			try {
				errors = evaluateAssertion(answerSets, assertion);
			} catch (Throwable t) {
				errors = List.of("Test execution failed due to internal error: " + t.getMessage());
				LOGGER.error("Test execution failed due to internal error", t);
			}
			if (!errors.isEmpty()) {
				failedCnt++;
			} else {
				passedCnt++;
			}

		}
		return Tests.newTestCaseResult(testCase.getName(), answerSetCountErrMsg, passedCnt, failedCnt, assertionErrors);
	}

	private List<String> evaluateAssertion(Set<AnswerSet> answerSets, Assertion assertion) {
		java.util.function.Predicate<AnswerSet> matcher = (as) -> this.answerSetSatisfiesAssertion(as, assertion);
		List<String> errorList = new ArrayList<>();
		switch (assertion.getMode()) {
			case FOR_ALL:
				if (!answerSets.stream().allMatch(matcher)) {
					errorList.addAll(
							answerSets.stream()
									.filter(matcher.negate())
									.map((as) -> "Universal assertion failed on answer set: " + as)
									.collect(Collectors.toSet())
					);
				}
				break;
			case FOR_SOME:
				if (answerSets.stream().noneMatch(matcher)) {
					errorList.add("No answer set matches existential assertion!");
				}
				break;
			default:
				throw new UnsupportedOperationException("Unsupported assertion mode: " + assertion.getMode());
		}
		return errorList;
	}

	private boolean answerSetSatisfiesAssertion(AnswerSet as, Assertion assertion) {
		ASPCore2Program verifierWithInput = Programs.builder(assertion.getVerifier()).addFacts(new ArrayList<>(as.asFacts())).build();
		return alpha.solve(verifierWithInput).findAny().isPresent();
	}

}
