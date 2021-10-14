/*
 * Copyright (c) 2021 Siemens AG
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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), c(N). [N@2]", negativeChoiceAtom)
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
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N,M) : a(N), d(M), c(N). [N@2]", negativeChoiceAtom)
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
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), c(N), not d(N). [N@2]"),
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), not d(N). [N@2]")
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
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), c(N), not d(N). [N@2]"),
				programPartParser.parseHeuristicDirective("#heuristic b(N) : a(N), not d(N). [N@2]")
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
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), c(N). [N@2]", negativeChoiceAtom),
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), not d(N). [N@2]", negativeChoiceAtom)
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
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), c(N). [N@2]", negativeChoiceAtom),
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), not d(N). [N@2]", negativeChoiceAtom)
		);
		assertEquals(expectedHeuristicDirectives, program.getInlineDirectives().getDirectives());
	}

	@Test
	public void testRuleWithAggregateAtom() {
		final InputProgram inputProgram = parser.parse("a(1). b(1)."
				+ "{ b(M) } :- a(M), b(X), X = #count { M : a(M) }."
				+ "#heuristic b(N) : a(N). [N@2]");
		final InputProgram program = new ChoiceHeadToNormal().apply(inputProgram);
		assertThrows(UnsupportedOperationException.class, () ->
				new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program));
	}

	@Test
	public void testRuleWithComparisonAtom() {
		InputProgram program = parser.parse("a(1). b(1)."
				+ "{ b(M) } :- a(M), b(X), M <= X."
				+ "#heuristic b(N) : a(N). [N@2]");
		final Atom negativeChoiceAtom = ChoiceHeadToNormal.constructNegativeChoiceAtom(programPartParser.parseBasicAtom("b(N)"));

		program = new ChoiceHeadToNormal().apply(program);
		program = new HeuristicDirectiveConditionEnhancement(heuristicsConfiguration).apply(program);
		Collection<HeuristicDirective> expectedHeuristicDirectives = Arrays.asList(
				parseHeuristicDirectiveAndAddNegativeLiteral("#heuristic b(N) : a(N), b(X), N <= X. [N@2]", negativeChoiceAtom)
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
