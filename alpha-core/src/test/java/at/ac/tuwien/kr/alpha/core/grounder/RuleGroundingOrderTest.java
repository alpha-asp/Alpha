/*
 * Copyright (c) 2017-2020, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
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
package at.ac.tuwien.kr.alpha.core.grounder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.parser.ProgramPartParser;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;

/**
 * Copyright (c) 2017-2019, the Alpha Team.
 */
// TODO this is a functional test that wants to be a unit test
public class RuleGroundingOrderTest {

	private static final ProgramParser PARSER = new ProgramParserImpl();
	private static final NormalizeProgramTransformation NORMALIZE_TRANSFORM = new NormalizeProgramTransformation(
			SystemConfig.DEFAULT_AGGREGATE_REWRITING_CONFIG);
	private static final Function<String, CompiledProgram> PARSE_AND_PREPROCESS = (str) -> {
		return InternalProgram.fromNormalProgram(NORMALIZE_TRANSFORM.apply(PARSER.parse(str)));
	};

	private static final ProgramPartParser PROGRAM_PART_PARSER = new ProgramPartParser();

	@Test
	public void groundingOrder() {
		// r1 := h(X,C) :- p(X,Y), q(A,B), r(Y,A), s(C).
		CompiledRule r1 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("h", 2), Terms.newVariable("X"), Terms.newVariable("C"))),
				Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newVariable("A"), Terms.newVariable("B")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("r", 2), Terms.newVariable("Y"), Terms.newVariable("A")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("s", 1), Terms.newVariable("C")).toLiteral());
		RuleGroundingInfo rgo1 = new RuleGroundingInfoImpl(r1);
		rgo1.computeGroundingOrders();
		assertEquals(4, rgo1.getStartingLiterals().size());

		// r2 := j(A,A,X,Y) :- r1(A,A), r1(X,Y), r1(A,X), r1(A,Y).
		CompiledRule r2 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("j", 4), Terms.newVariable("A"), Terms.newVariable("A"), Terms.newVariable("X"), Terms.newVariable("Y"))),
				Atoms.newBasicAtom(Predicates.getPredicate("r1", 2), Terms.newVariable("A"), Terms.newVariable("A")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("r1", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("r1", 2), Terms.newVariable("A"), Terms.newVariable("X")).toLiteral(),
				Atoms.newBasicAtom(Predicates.getPredicate("r1", 2), Terms.newVariable("A"), Terms.newVariable("Y")).toLiteral());
		RuleGroundingInfo rgo2 = new RuleGroundingInfoImpl(r2);
		rgo2.computeGroundingOrders();
		assertEquals(4, rgo2.getStartingLiterals().size());

		// r3 := p(a) :- b = a.
		CompiledRule r3 = new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("a"))),
				Atoms.newComparisonAtom(Terms.newSymbolicConstant("b"), Terms.newSymbolicConstant("a"), ComparisonOperators.EQ).toLiteral());
		RuleGroundingInfo rgo3 = new RuleGroundingInfoImpl(r3);
		rgo3.computeGroundingOrders();
		assertTrue(rgo3.hasFixedInstantiation());
	}

	@Test
	public void groundingOrderUnsafe() {
		assertThrows(RuntimeException.class, () -> {
			String aspStr = "h(X,C) :- X = Y, Y = C .. 3, C = X.";
			CompiledProgram prog = PARSE_AND_PREPROCESS.apply(aspStr);
			computeGroundingOrdersForRule(prog, 0);
		});
	}

	@Test
	public void testPositionFromWhichAllVarsAreBound_ground() {
		String aspStr = "a :- b, not c.";
		CompiledProgram internalPrg = PARSE_AND_PREPROCESS.apply(aspStr);
		RuleGroundingInfo rgo0 = computeGroundingOrdersForRule(internalPrg, 0);
		assertEquals(0, rgo0.getFixedGroundingOrder().getPositionFromWhichAllVarsAreBound());
	}

	@Test
	public void testPositionFromWhichAllVarsAreBound_simpleNonGround() {
		String aspStr = "a(X) :- b(X), not c(X).";
		CompiledProgram internalPrg = PARSE_AND_PREPROCESS.apply(aspStr);
		RuleGroundingInfo rgo0 = computeGroundingOrdersForRule(internalPrg, 0);
		assertEquals(1, rgo0.getStartingLiterals().size());
		for (Literal startingLiteral : rgo0.getStartingLiterals()) {
			assertEquals(0, rgo0.orderStartingFrom(startingLiteral).getPositionFromWhichAllVarsAreBound());
		}
	}

	@Test
	public void testPositionFromWhichAllVarsAreBound_longerSimpleNonGround() {
		String aspStr = "a(X) :- b(X), c(X), d(X), not e(X).";
		CompiledProgram internalPrg = PARSE_AND_PREPROCESS.apply(aspStr);
		RuleGroundingInfo rgo0 = computeGroundingOrdersForRule(internalPrg, 0);
		assertEquals(3, rgo0.getStartingLiterals().size());
		for (Literal startingLiteral : rgo0.getStartingLiterals()) {
			assertEquals(0, rgo0.orderStartingFrom(startingLiteral).getPositionFromWhichAllVarsAreBound());
		}
	}

	@Test
	public void testToString_longerSimpleNonGround() {
		String aspStr = "a(X) :- b(X), c(X), d(X), not e(X).";
		CompiledProgram internalPrg = PARSE_AND_PREPROCESS.apply(aspStr);
		RuleGroundingInfo rgo0 = computeGroundingOrdersForRule(internalPrg, 0);
		assertEquals(3, rgo0.getStartingLiterals().size());
		for (Literal startingLiteral : rgo0.getStartingLiterals()) {
			switch (startingLiteral.getPredicate().getName()) {
				case "b":
					assertEquals("b(X) : | c(X), d(X), not e(X)", rgo0.orderStartingFrom(startingLiteral).toString());
					break;
				case "c":
					assertEquals("c(X) : | b(X), d(X), not e(X)", rgo0.orderStartingFrom(startingLiteral).toString());
					break;
				case "d":
					assertEquals("d(X) : | b(X), c(X), not e(X)", rgo0.orderStartingFrom(startingLiteral).toString());
					break;
				default:
					fail("Unexpected starting literal: " + startingLiteral);
			}
		}
	}

	@Test
	public void testPositionFromWhichAllVarsAreBound_joinedNonGround() {
		String aspStr = "a(X) :- b(X), c(X,Y), d(X,Z), not e(X).";
		CompiledProgram internalPrg = PARSE_AND_PREPROCESS.apply(aspStr);
		RuleGroundingInfo rgo0 = computeGroundingOrdersForRule(internalPrg, 0);
		final Literal litBX = PROGRAM_PART_PARSER.parseLiteral("b(X)");
		final Literal litCXY = PROGRAM_PART_PARSER.parseLiteral("c(X,Y)");
		final Literal litDXZ = PROGRAM_PART_PARSER.parseLiteral("d(X,Z)");
		assertTrue(2 <= rgo0.orderStartingFrom(litBX).getPositionFromWhichAllVarsAreBound());
		assertTrue(1 <= rgo0.orderStartingFrom(litCXY).getPositionFromWhichAllVarsAreBound());
		assertTrue(1 <= rgo0.orderStartingFrom(litDXZ).getPositionFromWhichAllVarsAreBound());
	}

	private RuleGroundingInfo computeGroundingOrdersForRule(CompiledProgram program, int ruleIndex) {
		CompiledRule rule = program.getRules().get(ruleIndex);
		RuleGroundingInfo rgo = new RuleGroundingInfoImpl(rule);
		rgo.computeGroundingOrders();
		return rgo;
	}

}
