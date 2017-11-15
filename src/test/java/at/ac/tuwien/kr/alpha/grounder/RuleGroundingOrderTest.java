package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.VariableEqualityRemoval;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class RuleGroundingOrderTest {

	private final ProgramParser parser = new ProgramParser();


	@Test
	public void groundingOrder() throws IOException {
		Program program = parser.parse("h(X,C) :- p(X,Y), q(A,B), r(Y,A), s(C)." +
			"j(A,B,X,Y) :- r1(A,B), r1(X,Y), r1(A,X), r1(B,Y), A = B." +
			"p(a) :- b = a.");
		new VariableEqualityRemoval().transform(program);
		Rule rule0 = program.getRules().get(0);
		NonGroundRule nonGroundRule0 = NonGroundRule.constructNonGroundRule(new IntIdGenerator(), rule0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(nonGroundRule0);
		rgo0.computeGroundingOrders();
		assertEquals(4, rgo0.getStartingLiterals().size());

		Rule rule1 = program.getRules().get(1);
		NonGroundRule nonGroundRule1 = NonGroundRule.constructNonGroundRule(new IntIdGenerator(), rule1);
		RuleGroundingOrder rgo1 = new RuleGroundingOrder(nonGroundRule1);
		rgo1.computeGroundingOrders();
		assertEquals(4, rgo1.getStartingLiterals().size());

		Rule rule2 = program.getRules().get(2);
		NonGroundRule nonGroundRule2 = NonGroundRule.constructNonGroundRule(new IntIdGenerator(), rule2);
		RuleGroundingOrder rgo2 = new RuleGroundingOrder(nonGroundRule2);
		rgo2.computeGroundingOrders();
		assertTrue(rgo2.fixedInstantiation());
	}

	@Test(expected = RuntimeException.class)
	public void groundingOrderUnsafe() throws IOException {
		Program program = parser.parse("h(X,C) :- X = Y, Y = C .. 3, C = X.");
		new VariableEqualityRemoval().transform(program);
		Rule rule0 = program.getRules().get(0);
		NonGroundRule nonGroundRule0 = NonGroundRule.constructNonGroundRule(new IntIdGenerator(), rule0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(nonGroundRule0);
		rgo0.computeGroundingOrders();
	}

}