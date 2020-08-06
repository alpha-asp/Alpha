package at.ac.tuwien.kr.alpha.solver;

import org.junit.Test;

import java.io.IOException;

/**
 * Tests ASP programs containing arithmetic terms at arbitrary positions.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class ArithmeticTermsTest extends AbstractSolverTests {

	@Test
	public void testArithmeticTermInHead() throws IOException {
		String program = "dom(1). dom(2)."
			+ "p(X+3) :- dom(X).";
		assertAnswerSet(program, "dom(1),dom(2),p(4),p(5)");
	}

	@Test
	public void testArithmeticTermInRule() throws IOException {
		String program = "dom(1). dom(2)."
			+ "p(Y+4) :- dom(X+1), dom(X), Y=X, X=Y.";
		assertAnswerSet(program, "dom(1),dom(2),p(5)");
	}

	@Test
	public void testArithmeticTermInChoiceRule() throws IOException {
		String program = "cycle_max(4). cycle(1)." +
			"{ cycle(N+1) } :- cycle(N), cycle_max(K), N<K.";
		assertAnswerSetsWithBase(program, "cycle_max(4),cycle(1)", "", "cycle(2)", "cycle(2),cycle(3)", "cycle(2),cycle(3),cycle(4)");
	}

	@Test
	public void testMultipleArithmeticTermsInRules() throws IOException {
		String program = "q(1). q(3). r(f(40),6)." +
			"p(X+1) :- q(Y/2), r(f(X*2),Y), X-2 = Y*3, X = 0..20." +
			"bar(X,Y) :- q(Y/2), r(f(X*2),Y), X-2 = Y*3, X = 20.";
		assertAnswerSet(program, "q(1),q(3),r(f(40),6),p(21),bar(20,6)");
	}
}
