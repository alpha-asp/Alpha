package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class RuleTest {

	private final ProgramParser parser = new ProgramParser();

	@Test
	public void renameVariables() {
		String originalRule = "p(X,Y) :- a, f(Z) = 1, q(X,g(Y),Z), dom(A).";
		Rule rule = parser.parse(originalRule).getRules().get(0);
		Rule renamedRule = rule.renameVariables("_13");
		// FIXME: below test is unstable, better check for real equality.
		assertEquals("p(X_13, Y_13) :- a, f(Z_13) = 1, q(X_13, g(Y_13), Z_13), dom(A_13).", renamedRule.toString());
	}

}