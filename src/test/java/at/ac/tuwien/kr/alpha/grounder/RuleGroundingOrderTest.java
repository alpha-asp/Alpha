/**
 * Copyright (c) 2017-2018, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.VariableEqualityRemoval;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class RuleGroundingOrderTest {

	private final ProgramParser parser = new ProgramParser();

	@Test
	public void groundingOrder() {
		Program program = parser.parse("h(X,C) :- p(X,Y), q(A,B), r(Y,A), s(C)." +
				"j(A,B,X,Y) :- r1(A,B), r1(X,Y), r1(A,X), r1(B,Y), A = B." +
				"p(a) :- b = a.");
		new VariableEqualityRemoval().transform(program);
		Rule rule0 = program.getRules().get(0);
		NonGroundRule nonGroundRule0 = NonGroundRule.constructNonGroundRule(rule0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(nonGroundRule0);
		rgo0.computeGroundingOrders();
		assertEquals(4, rgo0.getStartingLiterals().size());

		Rule rule1 = program.getRules().get(1);
		NonGroundRule nonGroundRule1 = NonGroundRule.constructNonGroundRule(rule1);
		RuleGroundingOrder rgo1 = new RuleGroundingOrder(nonGroundRule1);
		rgo1.computeGroundingOrders();
		assertEquals(4, rgo1.getStartingLiterals().size());

		Rule rule2 = program.getRules().get(2);
		NonGroundRule nonGroundRule2 = NonGroundRule.constructNonGroundRule(rule2);
		RuleGroundingOrder rgo2 = new RuleGroundingOrder(nonGroundRule2);
		rgo2.computeGroundingOrders();
		assertTrue(rgo2.fixedInstantiation());
	}

	@Test(expected = RuntimeException.class)
	public void groundingOrderUnsafe() {
		Program program = parser.parse("h(X,C) :- X = Y, Y = C .. 3, C = X.");
		new VariableEqualityRemoval().transform(program);
		Rule rule0 = program.getRules().get(0);
		NonGroundRule nonGroundRule0 = NonGroundRule.constructNonGroundRule(rule0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(nonGroundRule0);
		rgo0.computeGroundingOrders();
	}

	@Test
	public void groundingOrderWithHeuristicGenerator() {
		Program program = parser.parse(
				"h(X,Y) :- x(X), y(Y). [2@2 : xy(X,Y)]"
						+ "x(1)."
						+ "{y(2)}."
						+ "xy(X,Y) :- x(X), y(Y).");

		Rule rule0 = program.getRules().get(0);
		NonGroundRule nonGroundRule0 = NonGroundRule.constructNonGroundRule(rule0);
		RuleGroundingOrder rgo0 = new RuleGroundingOrder(nonGroundRule0);
		rgo0.computeGroundingOrders();
		assertEquals(2, rgo0.getStartingLiterals().size());
		assertArrayEquals(
				new Literal[] {literal("y", variable("Y")), literal("xy", variable("X"), variable("Y"))},
				rgo0.orderStartingFrom(literal("x", variable("X"))));
		assertArrayEquals(
				new Literal[] {literal("x", variable("X")), literal("xy", variable("X"), variable("Y"))},
				rgo0.orderStartingFrom(literal("y", variable("Y"))));
	}

	private Literal literal(String predicate, Term... terms) {
		return new BasicAtom(Predicate.getInstance(predicate, terms.length), terms).toLiteral();
	}

	private VariableTerm variable(String name) {
		return VariableTerm.getInstance(name);
	}

}