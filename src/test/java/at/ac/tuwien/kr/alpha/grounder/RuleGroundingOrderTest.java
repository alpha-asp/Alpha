package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class RuleGroundingOrderTest {

	private final ProgramParser parser = new ProgramParser();


	@Test
	public void groundingOrder() throws IOException {
		Program program = parser.parse("h(X,C) :- p(X,Y), q(A,B), r(Y,A), s(C)." +
			"j(A,B,X,Y) :- r1(A,B), r1(X,Y), r1(A,X), r1(B,Y).");
		Rule rule = program.getRules().get(1);
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(new IntIdGenerator(), rule);
		RuleGroundingOrder rgo1 = new RuleGroundingOrder(nonGroundRule);
		rgo1.computeGroundingOrders();
		assertEquals(1, rgo1.getStartingLiterals().size());

		Rule rule2 = program.getRules().get(0);
		NonGroundRule nonGroundRule2 = NonGroundRule.constructNonGroundRule(new IntIdGenerator(), rule2);
		RuleGroundingOrder rgo2 = new RuleGroundingOrder(nonGroundRule2);
		rgo2.computeGroundingOrders();
		assertEquals(4, rgo2.getStartingLiterals().size());
	}

}