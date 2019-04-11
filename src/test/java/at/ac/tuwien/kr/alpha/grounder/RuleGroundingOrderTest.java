package at.ac.tuwien.kr.alpha.grounder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.impl.InternalRule;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class RuleGroundingOrderTest {

	@Test
	public void groundingOrder() throws IOException {
		Alpha system = new Alpha();
		InputProgram input = system.readProgramString(
				"h(X,C) :- p(X,Y), q(A,B), r(Y,A), s(C)." + "j(A,B,X,Y) :- r1(A,B), r1(X,Y), r1(A,X), r1(B,Y), A = B." + "p(a) :- b = a.", null);
		InternalProgram program = system.performProgramPreprocessing(input);

		InternalRule normalRule0 = program.getRules().get(0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(normalRule0);
		rgo0.computeGroundingOrders();
		assertEquals(4, rgo0.getStartingLiterals().size());

		InternalRule normalRule1 = program.getRules().get(1);
		RuleGroundingOrder rgo1 = new RuleGroundingOrder(normalRule1);
		rgo1.computeGroundingOrders();
		assertEquals(4, rgo1.getStartingLiterals().size());

		InternalRule normalRule2 = program.getRules().get(2);
		RuleGroundingOrder rgo2 = new RuleGroundingOrder(normalRule2);
		rgo2.computeGroundingOrders();
		assertTrue(rgo2.fixedInstantiation());
	}

	@Test(expected = RuntimeException.class)
	public void groundingOrderUnsafe() throws IOException {
		Alpha system = new Alpha();
		InputProgram input = system.readProgramString("h(X,C) :- X = Y, Y = C .. 3, C = X.", null);
		InternalProgram program = system.performProgramPreprocessing(input);

		InternalRule normalRule0 = program.getRules().get(0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(normalRule0);
		rgo0.computeGroundingOrders();
	}

}