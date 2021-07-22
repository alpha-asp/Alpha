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
package at.ac.tuwien.kr.alpha.grounder.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.externals.Externals;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.program.Programs;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.config.InputConfig;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

public class StratifiedEvaluationTest {

	@Test
	public void testDuplicateFacts() {
		String aspStr = "p(a). p(b). q(b). q(X) :- p(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Instance qOfB = new Instance(TestUtils.basicAtomWithSymbolicTerms("q", "b").getTerms());
		Set<Instance> facts = evaluated.getFactsByPredicate().get(at.ac.tuwien.kr.alpha.common.Predicate.getInstance("q", 1));
		int numQOfB = 0;
		for (Instance at : facts) {
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
		Atom equal = TestUtils.basicAtomWithSymbolicTerms("equal");
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
		String asp = "thing(1..3).\n" + "% choose exactly one - since Alpha doesn't support bounds,\n" + "% this needs two constraints\n"
				+ "{ chosenThing(X) : thing(X) }.\n" + "chosenSomething :- chosenThing(X).\n" + ":- not chosenSomething.\n"
				+ ":- chosenThing(X), chosenThing(Y), X != Y.\n" + "allThings :- 3 <= #count{ X : thing(X)}. \n"
				+ "chosenMaxThing :- allThings, chosenThing(3).\n" + ":- not chosenMaxThing.";
		Alpha system = new Alpha();
		// system.getConfig().setUseNormalizationGrid(true);
		InputProgram prg = system.readProgramString(asp);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		assertTrue(evaluated.getFacts().contains(TestUtils.basicAtomWithSymbolicTerms("allThings")));
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("allThings, thing(1), thing(2), thing(3), chosenMaxThing, chosenSomething, chosenThing(3)", answerSets);
	}

	@Test
	public void testNegatedFixedInterpretationLiteral() {
		String asp = "stuff(1). stuff(2). smallStuff(X) :- stuff(X), not X > 1.";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(asp);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("stuff(1), stuff(2), smallStuff(1)", answerSets);
	}

	@at.ac.tuwien.kr.alpha.api.externals.Predicate
	public static boolean sayTrue(Object o) {
		return true;
	}

	@Test
	public void testNegatedExternalLiteral() throws Exception {
		String asp = "claimedTruth(bla). truth(X) :- claimedTruth(X), &sayTrue[X]. lie(X) :- claimedTruth(X), not &sayTrue[X].";
		Alpha alpha = new Alpha();
		InputConfig inputCfg = InputConfig.forString(asp);
		inputCfg.addPredicateMethod("sayTrue", Externals.processPredicateMethod(this.getClass().getMethod("sayTrue", Object.class)));
		InputProgram input = alpha.readProgram(inputCfg);
		NormalProgram normal = alpha.normalizeProgram(input);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = alpha.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("claimedTruth(bla), truth(bla)", answerSets);
	}

	/**
	 * Tests an encoding associated with the partner units problem (PUP) that computes a topological order to be used by
	 * domain-specific heuristics. The entire program can be solved by stratified evaluation.
	 */
	@Test
	public void testPartnerUnitsProblemTopologicalOrder() throws IOException {
		Alpha system = new Alpha();
		InputProgram prg = new ProgramParser().parse(CharStreams.fromStream(this.getClass().getResourceAsStream("/partial-eval/pup_topological_order.asp")));
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new StratifiedEvaluation().apply(analyzed);
		assertTrue(evaluated.getRules().isEmpty(), "Not all rules eliminated by stratified evaluation");
		assertEquals(57, evaluated.getFacts().size());
	}

	/**
	 * Verifies correct handling of negated basic literals in StratifiedEvaluation.
	 * For details, see comments in test program
	 * 
	 * @throws IOException
	 */
	@Test
	public void testNegatedLiteralInRecursiveRule() throws IOException {
		//@formatter:off
		String expectedAnswerSet = "basefact1(1), basefact2(1), max_value(10), min_value(1), "
				+ "basefact1(9), basefact2(9), base(1), base(9), "
				+ "inc_value(1), inc_value(2), inc_value(2), inc_value(3), "
				+ "inc_value(4), inc_value(5), inc_value(6), inc_value(7), "
				+ "inc_value(8)";
		//@formatter:on
		InputProgram prog = Programs.fromInputStream(
				StratifiedEvaluationTest.class.getResourceAsStream("/partial-eval/recursive_w_negated_condition.asp"),
				new HashMap<>());
		Alpha systemStratified = new Alpha();
		systemStratified.getConfig().setEvaluateStratifiedPart(true);
		Set<AnswerSet> asStrat = systemStratified.solve(prog).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual(expectedAnswerSet, asStrat);
		Alpha systemNoStratEval = new Alpha();
		systemNoStratEval.getConfig().setEvaluateStratifiedPart(false);
		Set<AnswerSet> as = systemNoStratEval.solve(prog).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual(expectedAnswerSet, as);
	}

	@Test
	public void testRecursiveRanking() {
		//@formatter:off
		String asp = "thing(a).\n" + 
				"thing(b).\n" + 
				"thing(c).\n" + 
				"thing_before(a, b).\n" + 
				"thing_before(b, c).\n" + 
				"has_prev_thing(X) :- thing(X), thing_succ(_, X).\n" + 
				"first_thing(X) :- thing(X), not has_prev_thing(X).\n" + 
				"thing_not_succ(X, Y) :-\n" + 
				"	thing(X),\n" + 
				"	thing(Y),\n" + 
				"	thing(INTM),\n" + 
				"	thing_before(X, Y),\n" + 
				"	thing_before(X, INTM),\n" + 
				"	thing_before(INTM, X).\n" + 
				"thing_succ(X, Y) :-\n" + 
				"	thing(X),\n" + 
				"	thing(Y),\n" + 
				"	thing_before(X, Y),\n" + 
				"	not thing_not_succ(X, Y).\n" + 
				"thing_rank(X, 1) :- first_thing(X).\n" + 
				"thing_rank(X, R) :-\n" + 
				"	thing(X),\n" + 
				"	thing_succ(Y, X),\n" + 
				"	thing_rank(Y, K),\n" + 
				"	R = K + 1.";
		//@formatter:on
		Alpha alpha = new Alpha();
		InputProgram prog = alpha.readProgramString(asp);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(alpha.normalizeProgram(prog));
		StratifiedEvaluation evaluation = new StratifiedEvaluation();
		InternalProgram evaluated = evaluation.apply(analyzed);
		Predicate rank = Predicate.getInstance("thing_rank", 2);
		BasicAtom rank1 = new BasicAtom(rank, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getInstance(1));
		BasicAtom rank2 = new BasicAtom(rank, ConstantTerm.getSymbolicInstance("b"), ConstantTerm.getInstance(2));
		BasicAtom rank3 = new BasicAtom(rank, ConstantTerm.getSymbolicInstance("c"), ConstantTerm.getInstance(3));
		List<Atom> evaluatedFacts = evaluated.getFacts();
		assertTrue(evaluatedFacts.contains(rank1));
		assertTrue(evaluatedFacts.contains(rank2));
		assertTrue(evaluatedFacts.contains(rank3));
	}

}
