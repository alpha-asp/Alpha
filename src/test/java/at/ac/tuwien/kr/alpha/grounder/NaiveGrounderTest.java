/**
 * Copyright (c) 2018-2019 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.TestUtil.*;
import static org.junit.Assert.*;

/**
 * Tests {@link NaiveGrounder}
 */
public class NaiveGrounderTest {
	private static final ProgramParser PARSER = new ProgramParser();

	@Before
	public void resetRuleIdGenerator() {
		NonGroundRule.ID_GENERATOR.resetGenerator();
	}

	/**
	 * Asserts that a ground rule whose positive body is not satisfied by the empty assignment
	 * is grounded immediately.
	 */
	@Test
	public void groundRuleAlreadyGround() {
		Program program = PARSER.parse("a :- not b. "
				+ "b :- not a. "
				+ "c :- b.");

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		int litCNeg = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("c", 0))), false);
		int litB = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("b", 0))));
		assertExistsNoGoodContaining(noGoods.values(), litCNeg);
		assertExistsNoGoodContaining(noGoods.values(), litB);
	}

	/**
	 * Asserts that a ground rule whose positive non-unary body is not satisfied by the empty assignment
	 * is grounded immediately.
	 */
	@Test
	public void groundRuleWithLongerBodyAlreadyGround() {
		Program program = PARSER.parse("a :- not b. "
				+ "b :- not a. "
				+ "c :- b. "
				+ "d :- b, c. ");

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		int litANeg = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("a", 0))), false);
		int litBNeg = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("b", 0))), false);
		int litCNeg = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("c", 0))), false);
		int litDNeg = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("d", 0))), false);
		assertExistsNoGoodContaining(noGoods.values(), litANeg);
		assertExistsNoGoodContaining(noGoods.values(), litBNeg);
		assertExistsNoGoodContaining(noGoods.values(), litCNeg);
		assertExistsNoGoodContaining(noGoods.values(), litDNeg);
	}

	/**
	 * Asserts that a ground constraint whose positive body is not satisfied by the empty assignment
	 * is grounded immediately.
	 */
	@Test
	public void groundConstraintAlreadyGround() {
		Program program = PARSER.parse("a :- not b. "
				+ "b :- not a. "
				+ ":- b.");

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		int litB = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("b", 0))));
		assertTrue(noGoods.containsValue(NoGood.fromConstraint(Arrays.asList(litB), Collections.emptyList())));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void avoidDeadEndsWithLaxGrounderHeuristic() {
		RuleGroundingOrder groundingOrderP1 = new RuleGroundingOrder(literal("p1", "X"),
				new Literal[] {literal("p2", "X"), literal("q2", "Y"), literal("q1", "Y")}, -1);
		RuleGroundingOrder groundingOrderQ1 = new RuleGroundingOrder(literal("q1", "Y"),
				new Literal[] {literal("q2", "Y"), literal("p2", "X"), literal("p1", "X")}, -1);
		testDeadEnd(groundingOrderP1, groundingOrderQ1, false);
	}

	@Test
	public void noDeadEndWithLaxGrounderHeuristic() {
		RuleGroundingOrder groundingOrderP1 = new RuleGroundingOrder(literal("p1", "X"),
				new Literal[] {literal("p2", "X"), literal("q1", "Y"), literal("q2", "Y")}, -1);
		RuleGroundingOrder groundingOrderQ1 = new RuleGroundingOrder(literal("q1", "Y"),
				new Literal[] {literal("q2", "Y"), literal("p1", "X"), literal("p2", "X")}, -1);
		testDeadEnd(groundingOrderP1, groundingOrderQ1, true);
	}

	private void testDeadEnd(RuleGroundingOrder groundingOrderP1, RuleGroundingOrder groundingOrderQ1, boolean expectNoGoods) {
		Program program = PARSER.parse("p1(1). q1(1). "
				+ "x :- p1(X), p2(X), q1(Y), q2(Y). "
				+ "p2(X) :- something(X). " // this is to trick the grounder into believing that p2 and q2 might become true
				+ "q2(X) :- something(X). ");

		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = (NaiveGrounder) GrounderFactory.getInstance("naive", program, atomStore);

		NonGroundRule nonGroundRule = grounder.getNonGroundRule(0);
		nonGroundRule.groundingOrder.groundingOrders.put(literal("p1", "X"), groundingOrderP1);
		nonGroundRule.groundingOrder.groundingOrders.put(literal("q1", "Y"), groundingOrderQ1);

		TrailAssignment currentAssignment = new TrailAssignment(atomStore);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(currentAssignment);
		printNoGoods(atomStore, noGoods.values());
		assertEquals(expectNoGoods, !noGoods.isEmpty());
	}

	@Test
	public void testGroundingOfRuleSwitchedOffByFalsePositiveBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X). "
				+ "b(X) :- something(X). "); // this is to trick the grounder into believing that b might become true
		testIfGrounderGroundsRule(program, ThriceTruth.FALSE, false);
	}

	@Test
	public void testGroundingOfRuleNotSwitchedOffByTruePositiveBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X). "
				+ "b(X) :- something(X). "); // this is to trick the grounder into believing that b might become true
		testIfGrounderGroundsRule(program, ThriceTruth.TRUE, true);
	}

	@Test
	public void testGroundingOfRuleSwitchedOffByTrueNegativeBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), not b(X). "
				+ "b(X) :- something(X). "); // this is to trick the grounder into believing that b might become true
		testIfGrounderGroundsRule(program, ThriceTruth.TRUE, false);
	}

	@Test
	public void testGroundingOfRuleNotSwitchedOffByFalseNegativeBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), not b(X). "
				+ "b(X) :- something(X). "); // this is to trick the grounder into believing that b might become true
		testIfGrounderGroundsRule(program, ThriceTruth.FALSE, true);
	}

	private void testIfGrounderGroundsRule(Program program, ThriceTruth bTruth, boolean expectNoGoods) {
		AtomStore atomStore = new AtomStoreImpl();
		TrailAssignment currentAssignment = new TrailAssignment(atomStore);
		NaiveGrounder grounder = (NaiveGrounder) GrounderFactory.getInstance("naive", program, atomStore);

		int b = atomStore.putIfAbsent(atom("b", 1));
		currentAssignment.growForMaxAtomId();
		currentAssignment.assign(b, bTruth);

		Map<Integer, NoGood> noGoods = grounder.getNoGoods(currentAssignment);
		printNoGoods(atomStore, noGoods.values());
		assertEquals(expectNoGoods, !noGoods.isEmpty());
	}

	private void assertExistsNoGoodContaining(Collection<NoGood> noGoods, int literal) {
		for (NoGood noGood : noGoods) {
			for (int literalInNoGood : noGood) {
				if (literalInNoGood == literal) {
					return;
				}
			}
		}
		fail("No NoGood exists that contains literal " + literal);
	}

}
