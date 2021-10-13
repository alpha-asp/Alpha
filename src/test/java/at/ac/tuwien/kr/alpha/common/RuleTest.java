package at.ac.tuwien.kr.alpha.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class RuleTest {

	private final ProgramParser parser = new ProgramParser();

	@Test
	public void renameVariables() {
		String originalRule = "p(X,Y) :- a, f(Z) = 1, q(X,g(Y),Z), dom(A).";
		BasicRule rule = parser.parse(originalRule).getRules().get(0);
		InternalRule normalRule = InternalRule.fromNormalRule(NormalRule.fromBasicRule(rule));
		InternalRule renamedRule = normalRule.renameVariables("_13");
		BasicRule expectedRenamedRule = parser.parse("p(X_13, Y_13) :- a, f(Z_13) = 1, q(X_13, g(Y_13), Z_13), dom(A_13).").getRules().get(0);
		InternalRule expectedRenamedNormalRule = InternalRule.fromNormalRule(NormalRule.fromBasicRule(expectedRenamedRule));
		assertEquals(expectedRenamedNormalRule.toString(), renamedRule.toString());
	}

	@Test
	public void testRulesEqual() {
		InputProgram p1 = parser.parse("p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).");
		BasicRule r1 = p1.getRules().get(0);
		InputProgram p2 = parser.parse("p(X, Y) :- bla(X), blub(Y), foo(X, Y), not bar(X).");
		BasicRule r2 = p2.getRules().get(0);
		InputProgram p3 = parser.parse("p(X, Y) :- bla(X), blub(X), foo(X, X), not bar(X).");
		BasicRule r3 = p3.getRules().get(0);
		assertTrue(r1.equals(r2));
		assertTrue(r2.equals(r1));
		assertTrue(r1.hashCode() == r2.hashCode());
		assertFalse(r1.equals(r3));
		assertFalse(r3.equals(r1));
		assertTrue(r1.hashCode() != r3.hashCode());
		assertFalse(r2.equals(r3));
		assertFalse(r3.equals(r2));
		assertTrue(r2.hashCode() != r3.hashCode());
	}

}