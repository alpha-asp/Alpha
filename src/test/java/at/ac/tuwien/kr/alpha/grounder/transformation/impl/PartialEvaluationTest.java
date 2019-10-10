package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.impl.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.NormalProgram;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

public class PartialEvaluationTest {

	@Test
	public void testDuplicateFacts() {
		String aspStr = "p(a). p(b). q(b). q(X) :- p(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new PartialEvaluation().apply(analyzed);
		BasicAtom qOfB = BasicAtom.newInstance("q", "b");
		List<Atom> facts = evaluated.getFacts();
		int numQOfB = 0;
		for (Atom at : facts) {
			if (at.equals(qOfB)) {
				numQOfB++;
			}
		}
		Assert.assertEquals(1, numQOfB);
	}

	@Test
	public void testEqualityWithConstantTerms() {
		String aspStr = "equal :- 1 = 1.";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new PartialEvaluation().apply(analyzed);
		BasicAtom equal = BasicAtom.newInstance("equal");
		Assert.assertTrue(evaluated.getFacts().contains(equal));
	}
	
	
	@Test
	public void testEqualityWithVarTerms() {
		String aspStr = "a(1). a(2). a(3). b(X) :- a(X), X = 1. c(X) :- a(X), X = 2. d(X) :- X = 3, a(X).";
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		NormalProgram normal = system.normalizeProgram(prg);
		AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(normal);
		InternalProgram evaluated = new PartialEvaluation().apply(analyzed);
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
		InternalProgram evaluated = new PartialEvaluation().apply(analyzed);
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
		InternalProgram evaluated = new PartialEvaluation().apply(analyzed);
		Set<AnswerSet> answerSets = system.solve(evaluated).collect(Collectors.toSet());
		TestUtils.assertAnswerSetsEqual("a, b", answerSets);
	}
	
}
