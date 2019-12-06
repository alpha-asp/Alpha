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
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.heuristics.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static at.ac.tuwien.kr.alpha.TestUtil.atom;
import static at.ac.tuwien.kr.alpha.TestUtil.literal;
import static org.junit.Assert.*;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

/**
 * Tests {@link NaiveGrounder}
 * 
 * Some test cases use atoms of the something/1 predicate to trick the grounder
 * into believing that other atoms might become true. This is fragile because future implementations
 * of preprocessing techniques might render this trick useless.
 * If unit tests in this class begin to fail due to such improvements to preprocessing, this issue must be addressed.
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
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
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
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
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
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		int litB = Literals.atomToLiteral(atomStore.get(new BasicAtom(Predicate.getInstance("b", 0))));
		assertTrue(noGoods.containsValue(NoGood.fromConstraint(Collections.singletonList(litB), Collections.emptyList())));
	}

	@Test
	public void avoidDeadEndsWithLaxGrounderHeuristic() {
		RuleGroundingOrder groundingOrderP1 = new RuleGroundingOrder(literal("p1", "X"),
				Arrays.asList(literal("p2", "X"), literal("q2", "Y"), literal("q1", "Y")), -1, false);
		RuleGroundingOrder groundingOrderQ1 = new RuleGroundingOrder(literal("q1", "Y"),
				Arrays.asList(literal("q2", "Y"), literal("p2", "X"), literal("p1", "X")), -1, false);
		testDeadEnd(groundingOrderP1, groundingOrderQ1, true);
	}

	@Test
	public void noDeadEndWithLaxGrounderHeuristic() {
		RuleGroundingOrder groundingOrderP1 = new RuleGroundingOrder(literal("p1", "X"),
				Arrays.asList(literal("p2", "X"), literal("q1", "Y"), literal("q2", "Y")), -1, false);
		RuleGroundingOrder groundingOrderQ1 = new RuleGroundingOrder(literal("q1", "Y"),
				Arrays.asList(literal("q2", "Y"), literal("p1", "X"), literal("p2", "X")), -1, false);
		testDeadEnd(groundingOrderP1, groundingOrderQ1, true);
	}

	private void testDeadEnd(RuleGroundingOrder groundingOrderP1, RuleGroundingOrder groundingOrderQ1, boolean expectNoGoods) {
		Program program = PARSER.parse("p1(1). q1(1). "
				+ "x :- p1(X), p2(X), q1(Y), q2(Y). "
				+ "p2(X) :- something(X). "
				+ "q2(X) :- something(X). ");

		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = (NaiveGrounder) GrounderFactory.getInstance("naive", program, atomStore, p -> true, GrounderHeuristicsConfiguration.lax(), true);

		NonGroundRule nonGroundRule = grounder.getNonGroundRule(0);
		final Literal litP1X = literal("p1", "X");
		final Literal litQ1Y = literal("q1", "Y");
		nonGroundRule.groundingOrder.groundingOrders.put(litP1X, groundingOrderP1);
		nonGroundRule.groundingOrder.groundingOrders.put(litQ1Y, groundingOrderQ1);

		grounder.bootstrap();
		TrailAssignment currentAssignment = new TrailAssignment(atomStore);
		final Substitution substP1X1 = Substitution.unify(litP1X, new Instance(ConstantTerm.getInstance(1)), new Substitution());
		final Substitution substQ1Y1 = Substitution.unify(litQ1Y, new Instance(ConstantTerm.getInstance(1)), new Substitution());
		final NaiveGrounder.BindingResult bindingResultP1 = grounder.getGroundInstantiations(nonGroundRule, groundingOrderP1, substP1X1, currentAssignment);
		final NaiveGrounder.BindingResult bindingResultQ1 = grounder.getGroundInstantiations(nonGroundRule, groundingOrderQ1, substQ1Y1, currentAssignment);

		assertEquals(expectNoGoods, bindingResultP1.size() > 0);
		assertEquals(expectNoGoods, bindingResultQ1.size() > 0);
	}

	@Test
	public void testGroundingOfRuleSwitchedOffByFalsePositiveBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X). "
				+ "b(X) :- something(X). ");
		testIfGrounderGroundsRule(program, 0, literal("a", "X"), 1, ThriceTruth.FALSE, false);
	}

	@Test
	public void testGroundingOfRuleNotSwitchedOffByTruePositiveBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X). "
				+ "b(X) :- something(X). ");
		testIfGrounderGroundsRule(program, 0, literal("a", "X"), 1, ThriceTruth.TRUE, true);
	}

	@Test
	@Ignore("Currently, rule grounding is not switched off by a true negative body atom")
	public void testGroundingOfRuleSwitchedOffByTrueNegativeBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), not b(X). "
				+ "b(X) :- something(X). ");
		testIfGrounderGroundsRule(program, 0, literal("a", "X"), 1, ThriceTruth.TRUE, false);
	}

	@Test
	public void testGroundingOfRuleNotSwitchedOffByFalseNegativeBody() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), not b(X). "
				+ "b(X) :- something(X). ");

		testIfGrounderGroundsRule(program, 0, literal("a", "X"), 1, ThriceTruth.FALSE, true);
	}

	private void testIfGrounderGroundsRule(Program program, int ruleID, Literal startingLiteral, int startingInstance, ThriceTruth bTruth, boolean expectNoGoods) {
		AtomStore atomStore = new AtomStoreImpl();
		TrailAssignment currentAssignment = new TrailAssignment(atomStore);
		NaiveGrounder grounder = (NaiveGrounder) GrounderFactory.getInstance("naive", program, atomStore, p -> true, GrounderHeuristicsConfiguration.lax(), true);

		int b = atomStore.putIfAbsent(atom("b", 1));
		currentAssignment.growForMaxAtomId();
		currentAssignment.assign(b, bTruth);

		grounder.bootstrap();
		final NonGroundRule nonGroundRule = grounder.getNonGroundRule(ruleID);
		final Substitution substStartingLiteral = Substitution.unify(startingLiteral, new Instance(ConstantTerm.getInstance(startingInstance)), new Substitution());
		final NaiveGrounder.BindingResult bindingResult = grounder.getGroundInstantiations(nonGroundRule, nonGroundRule.groundingOrder.groundingOrders.get(startingLiteral), substStartingLiteral, currentAssignment);
		assertEquals(expectNoGoods, bindingResult.size() > 0);
	}
	
	@Test
	public void testLaxGrounderHeuristicTolerance_0_reject() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X). "
				+ "b(X) :- something(X).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 0, 0, false, Arrays.asList(1));
	}
	
	@Test
	public void testLaxGrounderHeuristicTolerance_1_accept() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X). "
				+ "b(X) :- something(X).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 1, 0, true, Arrays.asList(1));
	}
	
	@Test
	public void testLaxGrounderHeuristicTolerance_1_reject() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X), b(X+1). "
				+ "b(X) :- something(X).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 1, 0, false, Arrays.asList(2));
	}

	@Test
	public void testLaxGrounderHeuristicTolerance_2_accept() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X), b(X+1). "
				+ "b(X) :- something(X).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 2, 0, true, Arrays.asList(2));
	}

	@Test
	public void testLaxGrounderHeuristicTolerance_1_accept_two_substitutions() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X,Y). "
				+ "b(X,Y) :- something(X,Y).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 1, new ThriceTruth[] {TRUE, TRUE}, 2, true, Arrays.asList(0, 0));
	}

	@Test
	public void testLaxGrounderHeuristicTolerance_1_accept_accept_two_substitutions_with_different_remaining_tolerances() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(1), b(X,Y). "
				+ "b(X,Y) :- something(X,Y).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", 1), 1, 1, new ThriceTruth[] {null, null}, 2, true, Arrays.asList(1, 1));
	}

	@Test
	public void testLaxGrounderHeuristicTolerance_2_reject() {
		Program program = PARSER.parse("a(1). "
				+ "c(X) :- a(X), b(X), b(X+1), b(X+2). "
				+ "b(X) :- something(X).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 2, 0, false, Arrays.asList(3));
	}

	@Test
	public void testLaxGrounderHeuristicTolerance_2_accept_multiple_facts_of_same_variable() {
		Program program = PARSER.parse("a(1). b(1). "
				+ "c(X) :- a(X), b(X), b(X+1), b(X+2). "
				+ "b(X) :- something(X).");
		testLaxGrounderHeuristicTolerance(program, 0, literal("a", "X"), 1, 2, 0, true, Arrays.asList(2));
	}

	private void testLaxGrounderHeuristicTolerance(Program program, int ruleID, Literal startingLiteral, int startingInstance, int tolerance, int nTrueBs, boolean expectNoGoods, List<Integer> expectedNumbersOfUnassignedPositiveBodyAtoms) {
		ThriceTruth[] truthsOfB = new ThriceTruth[nTrueBs];
		for (int i = 0; i < nTrueBs; i++) {
			truthsOfB[i] = ThriceTruth.TRUE;
		}
		testLaxGrounderHeuristicTolerance(program, ruleID, startingLiteral, startingInstance, tolerance, truthsOfB, 1, expectNoGoods, expectedNumbersOfUnassignedPositiveBodyAtoms);
	}

	private void testLaxGrounderHeuristicTolerance(Program program, int ruleID, Literal startingLiteral, int startingInstance, int tolerance, ThriceTruth[] truthsOfB, int arityOfB, boolean expectNoGoods, List<Integer> expectedNumbersOfUnassignedPositiveBodyAtoms) {
		AtomStore atomStore = new AtomStoreImpl();
		TrailAssignment currentAssignment = new TrailAssignment(atomStore);
		GrounderHeuristicsConfiguration heuristicConfiguration = GrounderHeuristicsConfiguration.getInstance(tolerance, tolerance);
		NaiveGrounder grounder = (NaiveGrounder) GrounderFactory.getInstance("naive", program, atomStore, p -> true, heuristicConfiguration, true);

		int[] bAtomIDs = new int[truthsOfB.length];
		for (int i = 0; i < truthsOfB.length; i++) {
			int[] bTerms = new int[arityOfB];
			for (int n = 0; n < arityOfB; n++) {
				bTerms[n] = (n == arityOfB - 1) ? i + 1 : startingInstance;
			}
			bAtomIDs[i] = atomStore.putIfAbsent(atom("b", bTerms));
		}
		addAtomsToWorkingMemoryWithoutChangingTheAssignment(atomStore, grounder, bAtomIDs);
		assign(currentAssignment, bAtomIDs, truthsOfB);

		grounder.bootstrap();
		final NonGroundRule nonGroundRule = grounder.getNonGroundRule(ruleID);
		final Substitution substStartingLiteral = Substitution.unify(startingLiteral, new Instance(ConstantTerm.getInstance(startingInstance)), new Substitution());
		final NaiveGrounder.BindingResult bindingResult = grounder.getGroundInstantiations(nonGroundRule, nonGroundRule.groundingOrder.groundingOrders.get(startingLiteral), substStartingLiteral, currentAssignment);
		assertEquals(expectNoGoods, bindingResult.size() > 0);
		if (bindingResult.size() > 0) {
			assertEquals(expectedNumbersOfUnassignedPositiveBodyAtoms, bindingResult.numbersOfUnassignedPositiveBodyAtoms);
		} else {
			assertTrue(bindingResult.numbersOfUnassignedPositiveBodyAtoms.isEmpty());
		}
	}

	private void assign(TrailAssignment currentAssignment, int[] atomIDs, ThriceTruth[] truthValues) {
		currentAssignment.growForMaxAtomId();
		for (int i = 0; i < truthValues.length; i++) {
			int atomID = atomIDs[i];
			if (truthValues[i] != null) {
				currentAssignment.assign(atomID, truthValues[i]);
			}
		}
	}

	private void addAtomsToWorkingMemoryWithoutChangingTheAssignment(AtomStore atomStore, NaiveGrounder grounder, int[] atomIDs) {
		TrailAssignment temporaryAssignment = new TrailAssignment(atomStore);
		temporaryAssignment.growForMaxAtomId();
		for (int b : atomIDs) {
			temporaryAssignment.assign(b, TRUE);
		}
		grounder.updateAssignment(temporaryAssignment.getNewPositiveAssignmentsIterator());
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
