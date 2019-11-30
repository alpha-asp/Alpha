package at.ac.tuwien.kr.alpha.common;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.impl.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;
import at.ac.tuwien.kr.alpha.common.rule.impl.NormalRule;
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
		Assert.assertTrue(r1.equals(r2));
		Assert.assertTrue(r2.equals(r1));
		Assert.assertFalse(r1.equals(r3));
		Assert.assertFalse(r3.equals(r1));
		Assert.assertFalse(r2.equals(r3));
		Assert.assertFalse(r3.equals(r2));
	}

}