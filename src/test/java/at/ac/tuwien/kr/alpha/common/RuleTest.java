package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.rule.impl.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.impl.NormalRule;
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
		BasicRule rule = parser.parse(originalRule).getRules().get(0);
		NormalRule normalRule = NormalRule.fromBasicRule(rule);
		NormalRule renamedRule = normalRule.renameVariables("_13");
		BasicRule expectedRenamedRule = parser.parse("p(X_13, Y_13) :- a, f(Z_13) = 1, q(X_13, g(Y_13), Z_13), dom(A_13).").getRules().get(0);
		NormalRule expectedRenamedNormalRule = NormalRule.fromBasicRule(expectedRenamedRule);
		assertEquals(expectedRenamedNormalRule.toString(), renamedRule.toString());
		// FIXME: Rule does not implement equals()
	}

}