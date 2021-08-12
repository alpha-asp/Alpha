package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;
import org.junit.Test;

import java.util.Set;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class WeakConstraintsTests extends AbstractSolverTests {

	@Test
	public void simpleWeightsSameLevel() {
		String program = ":~a.[1@0,foo,bar]" +
			":~b.[2@0,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals("a", "1@0", actualAnswerSets);
	}


	@Test
	public void simpleWeightedAnswerSet() {
		String program = ":~a.[2@2,foo,bar]" +
			":~b.[1@1,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals("b", "1@1", actualAnswerSets);
	}

	@Test
	public void simpleWeightedAnswerSetWithNegativeLevel() {
		String program = ":~a.[2@1,foo,bar]" +
			":~b.[1@-1,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals("b", "1@-1", actualAnswerSets);
	}

	@Test
	public void simpleMultiLevelWeightedAnswerSet() {
		String program = ":~a.[2@2,foo,bar]" +
			":~b.[1@1,baz]" +
			":~b.[3@-4,baz]" +
			"a :- not b. b:- not a.";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals("b", "3@-4, 1@1", actualAnswerSets);
	}

	@Test
	public void sameWeightSummedUpInLevel() {
		String program = "{a;b}." +
			":- not a, not b." +
			":~b.[1@3]" +
			":~a.[2@1,foo]" +
			":~a.[2@1,bar]";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals("a", "4@1", actualAnswerSets);
	}

	@Test
	public void sameWeightSameTermNotSummedUpInLevel() {
		String program = "{a;b}." +
			":- not a, not b." +
			":~b.[1@3]" +
			":~a.[2@1,foo]" +
			":~a.[2@1,foo]";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals("a", "2@1", actualAnswerSets);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeWeightThrowsException() {
		String program = "p(1..9)." +
			"a :- q(X)." +
			"{ q(X) } :- p(X)." +
			"has_q :- q(X)." +
			":- not has_q." +
			":- q(X), q(Y), X != Y." +
			"w(Z) :- Z = 8 - K, q(K)." +
			":~a,w(Z).[Z@1]";
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		// In case negative weights can be dealt with (e.g. by fully grounding or under certain restrictions),
		// the optimum answer set of above program is: p(1),...,p(9),q(9),w(-1),a,has_q at valuation -1@1
		// Under current behaviour we expect the computation of answer-sets to fail already.
		TestUtils.assertOptimumAnswerSetEquals(
			"p(1),p(2),p(3),p(4),p(5),p(6),p(7),p(8),p(9),q(9),w(-1),a,has_q", "-1@1", actualAnswerSets);
	}

	@Test
	public void complexValuationWithMultipleWeightsOnMultipleLevels() {
		String program = "dom(1..3)." +
			"{ a(X) } :- dom(X)." +
			"{ b(X) } :- dom(X)." +
			"{ c(X) } :- dom(X)." +
			"weightatlevel(W,L) :- a(X),b(Y),c(Z), W = 2*X+Y, L = Z*Y." +
			":~ weightatlevel(W,L).[W@L]" +
			"has_wal :- weightatlevel(W,L)." +
			":- not has_wal.";
		System.out.println(program);
		Set<AnswerSet> actualAnswerSets = collectSet(program);
		TestUtils.assertOptimumAnswerSetEquals(
			"dom(1), dom(2), dom(3), c(1), b(1), a(1), weightatlevel(3,1), has_wal", "3@1", actualAnswerSets);
	}
}
