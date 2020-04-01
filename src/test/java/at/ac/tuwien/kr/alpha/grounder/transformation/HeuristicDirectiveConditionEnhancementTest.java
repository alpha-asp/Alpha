/*
 * Copyright (c) 2021-2022 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.common.EnumerationDirective;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveAtom;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveBody;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramPartParser;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfigurationBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link HeuristicDirectiveConditionEnhancement}.
 */
public class HeuristicDirectiveConditionEnhancementTest {
	private final ProgramParser parser = new ProgramParser();
	private final ProgramPartParser programPartParser = new ProgramPartParser();
	private final HeuristicsConfiguration heuristicsConfiguration = new HeuristicsConfigurationBuilder().setRespectDomspecHeuristics(true).build();

	@Test
	public void testEnhancementOfOneDirectiveWithoutUnification() {
		InputProgram program = parser.parse("a(1). c(1). d(1)."
				+ "{ b(N) } :- c(N)."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T c(N). [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testEnhancementOfOneDirectiveWithPartialUnification() {
		InputProgram program = parser.parse("a(1). c(1). d(1)."
				+ "{ b(N,X) } :- c(N), d(X)."
				+ "#heuristic b(N,M) : a(N), d(M). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N,M)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N,M) : a(N), d(M), T c(N), T d(M). [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testEnhancementOfOneDirectiveByTwoNormalRulesWithoutUnification() {
		InputProgram program = parser.parse("a(1). c(1). d(1)."
				+ "b(N) :- c(N), not d(N)."
				+ "b(N) :- a(N), not d(N)."
				+ "#heuristic b(N) : a(N). [N@2]");

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), T c(N), not d(N). [N@2]"),
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), T a(N), not d(N). [N@2]")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testEnhancementOfOneDirectiveByTwoNormalRules() {
		InputProgram program = parser.parse("a(1). c(1). d(1)."
				+ "b(M) :- c(M), not d(M)."
				+ "b(L) :- a(L), not d(L)."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), T c(N), not d(N). [N@2]"),
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), T a(N), not d(N). [N@2]")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testEnhancementOfOneDirectiveByTwoChoiceRulesWithoutUnification() {
		InputProgram program = parser.parse("a(1). c(1). d(1)."
				+ "{ b(N) } :- c(N)."
				+ "{ b(N) } :- a(N), not d(N)."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T c(N). [N@2]", negativeChoiceAtom),
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T a(N), not d(N). [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testEnhancementOfOneDirectiveByTwoChoiceRules() {
		InputProgram program = parser.parse("a(1). c(1). d(1)."
				+ "{ b(M) } :- c(M)."
				+ "{ b(L) } :- a(L), not d(L)."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T c(N). [N@2]", negativeChoiceAtom),
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T a(N), not d(N). [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testRuleWithAggregateAtom() {
		InputProgram program = parser.parse("a(1). b(1). p(1). q(1,1). q(1,2)."
				+ "{ b(X) } :- a(X), b(X), X = #sum { Y : p(Y) }, 1 < #count { Z : q(X,Z) }."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T a(N), T b(N), N = #sum { Y : p(Y) }, 1 < #count { Z : q(N,Z) }. [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testRuleWithComparisonAtom() {
		InputProgram program = parser.parse("a(1). b(1)."
				+ "{ b(M) } :- a(M), b(X), M <= X."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), T a(N), T b(X), N <= X. [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testUnificationWithConstantTerm() {
		InputProgram program = parser.parse("elem(d,1). comUnit(1)."
				+ "{ gt(A,X,U) } :- elem(A,X), comUnit(U)."
				+ "assign(1,d,D) :- elem(d,D), comUnit(1), not gt(d,D,1)."
				+ "#heuristic assign(U,T,X) : T comUnit(U), T elem(T,X).");

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				programPartParser.parseHeuristicDirective("#heuristic assign(1,d,X) : T comUnit(1), T elem(d,X), not gt(d,X,1).")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testUnificationWithArithmeticTerm_SharedVariables() {
		InputProgram program = parser.parse("elem(d,1). comUnit(1)."
				+ "{ gt(A,X,U) } :- elem(A,X), comUnit(U)."
				+ "assign(U+1,d,D) :- elem(d,D), comUnit(U), not gt(d,D,U+1)."
				+ "#heuristic assign(U,T,X) : T comUnit(U), T elem(T,X).");

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				programPartParser.parseHeuristicDirective("#heuristic assign(U_1+1,d,X) : T comUnit(U_1), T elem(d,X), T comUnit(U_1+1), not gt(d,X,U_1+1).")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testUnificationWithArithmeticTerm() {
		InputProgram program = parser.parse("elem(d,1). comUnit(1)."
				+ "{ gt(A,X,U) } :- elem(A,X), comUnit(U)."
				+ "assign(U1,d,D) :- elem(d,D), comUnit(U), comUnit(U1), U1=U+1, not gt(d,D,U1)."
				+ "#heuristic assign(Un,T,X) : T comUnit(Un), T elem(T,X).");

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				programPartParser.parseHeuristicDirective("#heuristic assign(Un,d,X) : T elem(d,X), T comUnit(Un), T comUnit(U), Un=U+1, not gt(d,X,Un).")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testUnificationWithFunctionTerm() {
		InputProgram program = parser.parse("legacyConfig(cabinetTOthing(1,2)). legacyConfig(roomTOcabinet(3,1))."
				+ "reuse(cabinetTOthing(X,Y))  :- legacyConfig(cabinetTOthing(X,Y)), not delete(cabinetTOthing(X,Y))."
				+ "delete(cabinetTOthing(X,Y)) :- legacyConfig(cabinetTOthing(X,Y)), not  reuse(cabinetTOthing(X,Y))."
				+ "reuse(roomTOcabinet(X,Y))  :- legacyConfig(roomTOcabinet(X,Y)), not delete(roomTOcabinet(X,Y))."
				+ "delete(roomTOcabinet(X,Y)) :- legacyConfig(roomTOcabinet(X,Y)), not  reuse(roomTOcabinet(X,Y))."
				+ "#heuristic reuse(cabinetTOthing(C,T)) : T legacyConfig(cabinetTOthing(C,T)), not thingLong(T).");

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				programPartParser.parseHeuristicDirective("#heuristic reuse(cabinetTOthing(C,T)) : T legacyConfig(cabinetTOthing(C,T)), not TM delete(cabinetTOthing(C,T)), not TM thingLong(T).")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testBidirectionalUnificationWithFunctionTerm() {
		InputProgram program = parser.parse("b(fn1(1,2),fn2(3,4))."
				+ "h(Y,fn2(C,D)) :- b(Y,fn2(C,D))."
				+ "#heuristic h(fn1(A,B),X).");

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Collections.singletonList(
				programPartParser.parseHeuristicDirective("#heuristic h(fn1(A,B),fn2(C,D)) : T b(fn1(A,B),fn2(C,D)).")
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testOtherDirectivesAreUnaffected() {
		InputProgram program = parser.parse("#enumeration_predicate_is a.");
		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		assertEquals(1, program.getInlineDirectives().getDirectives().size());
		assertEquals("a", ((EnumerationDirective)program.getInlineDirectives().getDirectives().iterator().next()).getValue());
	}

	private HeuristicDirective parseHeuristicDirectiveAndAddNegativeLiteral(String strHeuristicDirective, Atom negativeChoiceAtom) {
		final HeuristicDirective parsedHeuristicDirective = programPartParser.parseHeuristicDirective(strHeuristicDirective);
		final Collection<HeuristicDirectiveAtom> extendedHeuristicDirectiveBodyNegative = new LinkedHashSet<>(parsedHeuristicDirective.getBody().getBodyAtomsNegative());
		extendedHeuristicDirectiveBodyNegative.add(HeuristicDirectiveAtom.body(negativeChoiceAtom));
		final HeuristicDirectiveBody extendedHeuristicDirectiveBody = new HeuristicDirectiveBody(parsedHeuristicDirective.getBody().getBodyAtomsPositive(), extendedHeuristicDirectiveBodyNegative);
		return new HeuristicDirective(parsedHeuristicDirective.getHead(), extendedHeuristicDirectiveBody, parsedHeuristicDirective.getWeightAtLevel());
	}

}
