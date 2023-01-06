package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

import java.util.Set;

import static at.ac.tuwien.kr.alpha.test.util.TestUtils.assertOptimumAnswerSetEquals;
import static at.ac.tuwien.kr.alpha.test.util.TestUtils.collectRegressionTestAnswerSets;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeakConstraintsTests {

	@RegressionTest
	public void simpleWeightsSameLevel(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = ":~a.[1@0,foo,bar]" +
			":~b.[2@0,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals("a", "1@0", actualAnswerSets);
	}


	@RegressionTest
	public void simpleWeightedAnswerSet(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = ":~a.[2@2,foo,bar]" +
			":~b.[1@1,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals("b", "1@1", actualAnswerSets);
	}

	@RegressionTest
	public void simpleWeightedAnswerSetWithNegativeLevel(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = ":~a.[2@1,foo,bar]" +
			":~b.[1@-1,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals("b", "1@-1", actualAnswerSets);
	}

	@RegressionTest
	public void simpleMultiLevelWeightedAnswerSet(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = ":~a.[2@2,foo,bar]" +
			":~b.[1@1,baz]" +
			":~b.[3@-4,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals("b", "3@-4, 1@1", actualAnswerSets);
	}

	@RegressionTest
	public void sameWeightSummedUpInLevel(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = "{a;b}." +
			":- not a, not b." +
			":~b.[1@3]" +
			":~a.[2@1,foo]" +
			":~a.[2@1,bar]";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals("a", "4@1", actualAnswerSets);
	}

	@RegressionTest
	public void sameWeightSameTermNotSummedUpInLevel(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = "{a;b}." +
			":- not a, not b." +
			"c :- a." +
			":~b.[1@3]" +
			":~a.[2@1,foo]" +
			":~c.[2@1,foo]";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals("a, c", "2@1", actualAnswerSets);
	}

	@RegressionTest
	public void negativeWeightThrowsException(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = "p(1..9)." +
			"a :- q(X)." +
			"{ q(X) } :- p(X), X != 8. % exclude case where violation is 0, which could be encountered before negative weight.\n" +
			"has_q :- q(X)." +
			":- not has_q." +
			":- q(X), q(Y), X != Y." +
			"w(Z) :- Z = 8 - K, q(K)." +
			":~a,w(Z).[Z@1]";
		assertThrows(IllegalArgumentException.class, () -> {
			Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
			// In case negative weights can be dealt with (e.g. by fully grounding or under certain restrictions),
			// the optimum answer set of above program is: p(1),...,p(9),q(9),w(-1),a,has_q at valuation -1@1
			// Under current behaviour we expect the computation of answer-sets to fail already.
			assertOptimumAnswerSetEquals(
				"p(1),p(2),p(3),p(4),p(5),p(6),p(7),p(8),p(9),q(9),w(-1),a,has_q", "-1@1", actualAnswerSets);
		});
	}

	@RegressionTest
	public void complexValuationWithMultipleWeightsOnMultipleLevels(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		String program = "dom(1..3)." +
			"{ a(X) } :- dom(X)." +
			"{ b(X) } :- dom(X)." +
			"{ c(X) } :- dom(X)." +
			"weightatlevel(W,L) :- a(X),b(Y),c(Z), W = 2*X+Y, L = Z*Y." +
			":~ weightatlevel(W,L).[W@L]" +
			"has_wal :- weightatlevel(W,L)." +
			":- not has_wal.";
		Set<AnswerSet> actualAnswerSets = collectRegressionTestAnswerSets(program, cfg);
		assertOptimumAnswerSetEquals(
			"dom(1), dom(2), dom(3), c(1), b(1), a(1), weightatlevel(3,1), has_wal", "3@1", actualAnswerSets);
	}
}
