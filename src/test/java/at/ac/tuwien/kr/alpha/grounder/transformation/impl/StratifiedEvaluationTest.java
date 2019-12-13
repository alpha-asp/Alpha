/**
 * Copyright (c) 2019, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.impl.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.NormalProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;
import org.antlr.v4.runtime.CharStreams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StratifiedEvaluationTest {

	@Test
	public void testDuplicateFacts() {
		String aspStr = "p(a). p(b). q(b). q(X) :- p(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		BasicAtom qOfB = BasicAtom.newInstance("q", "b");
		List<Atom> facts = evaluated.getFacts();
		int numQOfB = 0;
		for (Atom at : facts) {
			if (at.equals(qOfB)) {
				numQOfB++;
			}
		}
		assertEquals(1, numQOfB);
	}

	@Test
	public void testEqualityWithConstantTerms() {
		String aspStr = "equal :- 1 = 1.";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		BasicAtom equal = BasicAtom.newInstance("equal");
		assertTrue(evaluated.getFacts().contains(equal));
	}
	
	
	@Test
	public void testEqualityWithVarTerms() {
		String aspStr = "a(1). a(2). a(3). b(X) :- a(X), X = 1. c(X) :- a(X), X = 2. d(X) :- X = 3, a(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("a(1), a(2), a(3), b(1), c(2), d(3)", answerSets);
	}
	
	@Test
	public void testNonGroundableRule() {
		String asp = "p(a). q(a, b). s(X, Y) :- p(X), q(X, Y), r(Y).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(asp);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("p(a), q(a,b)", answerSets);
	}

	@Test
	public void testCountAggregate() {
		String asp = "a. b :- 1 <= #count { 1 : a }.";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(asp);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("a, b", answerSets);
	}
	
	@Test
	public void testIntervalFact() {
		String asp = "a(1..3).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(asp);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("a(1), a(2), a(3)", answerSets);
	}
	
	@Test
	public void testAggregateSpecial() {
		String asp = "thing(1..3).\n" + 
				"% choose exactly one - since Alpha doesn't support bounds,\n" + 
				"% this needs two constraints\n" + 
				"{ chosenThing(X) : thing(X) }.\n" + 
				"chosenSomething :- chosenThing(X).\n" + 
				":- not chosenSomething.\n" + 
				":- chosenThing(X), chosenThing(Y), X != Y.\n" + 
				"allThings :- 3 <= #count{ X : thing(X)}. \n" + 
				"chosenMaxThing :- allThings, chosenThing(3).\n" + 
				":- not chosenMaxThing.";
		Alpha system = new Alpha();
		//system.getConfig().setUseNormalizationGrid(true);
		InputProgram prg = system.readProgramString(asp);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		assertTrue(evaluated.getFacts().contains(BasicAtom.newInstance("allThings")));
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("allThings, thing(1), thing(2), thing(3), chosenMaxThing, chosenSomething, chosenThing(3)", answerSets);
	}

	/**
	 * Tests an encoding associated with the partner units problem (PUP) that computes a topolical order to be used by
	 * domain-specific heuristics. The entire program can be solved by stratified evaluation.
	 */
	@Test
	public void testPartnerUnitsProblemTopologicalOrder() throws IOException {
		Alpha system = new Alpha();
		InputProgram prg = new ProgramParser()
				.parse(CharStreams.fromStream(this.getClass().getResourceAsStream("/partial-eval/pup_topological_order.asp")));
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		assertTrue("Not all rules eliminated by stratified evaluation", evaluated.getRules().isEmpty());
		assertEquals(57, evaluated.getFacts().size());
	}
	
}
