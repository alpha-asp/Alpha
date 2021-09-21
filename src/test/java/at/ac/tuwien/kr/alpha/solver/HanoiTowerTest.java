/**
 * Copyright (c) 2017-2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.solver;

import static at.ac.tuwien.kr.alpha.test.util.TestUtils.runWithTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.SortedSet;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

/**
 * Tests {@link AbstractSolver} using some hanoi tower test cases (see https://en.wikipedia.org/wiki/Tower_of_Hanoi).
 *
 */
public class HanoiTowerTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(HanoiTowerTest.class);

	private static final int DEBUG_TIMEOUT_FACTOR = 5;
	
	private final ProgramParser parser = new ProgramParser();

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance1(RegressionTestConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(1, cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance2(RegressionTestConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(2, cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance3(RegressionTestConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(3, cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance4(RegressionTestConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(4, cfg));
	}

	@RegressionTest
	public void testSimple(RegressionTestConfig cfg) {
		TestUtils.ignoreTestForNaiveSolver(cfg);
		TestUtils.ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		long timeout = 60000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower("simple", cfg));
	}

	private void testHanoiTower(int instance, RegressionTestConfig cfg) throws IOException {
		testHanoiTower(String.valueOf(instance), cfg);
	}

	private void testHanoiTower(String instance, RegressionTestConfig cfg) throws IOException {
		InputProgram.Builder programBuilder = InputProgram.builder();
		programBuilder.accumulate(parser.parse(CharStreams.fromPath(Paths.get("src", "test", "resources", "HanoiTower_Alpha.asp"))));
		programBuilder.accumulate(parser.parse(CharStreams.fromPath(Paths.get("src", "test", "resources", "HanoiTower_instances", instance + ".asp"))));
		InputProgram program = programBuilder.build();
		Solver solver = TestUtils.buildSolverForRegressionTest(program, cfg);
		Optional<AnswerSet> answerSet = solver.stream().findFirst();
		assertTrue(answerSet.isPresent());
		checkGoal(program, answerSet.get());
	}

	/**
	 * Conducts a very simple, non-comprehensive goal check (i.e. it may classify answer sets as correct that are actually wrong) by checking if for every goal/3
	 * fact in the input there is a corresponding on/3 atom in the output.
	 */
	private void checkGoal(InputProgram parsedProgram, AnswerSet answerSet) {
		Predicate ongoal = Predicate.getInstance("ongoal", 2);
		Predicate on = Predicate.getInstance("on", 3);
		int steps = getSteps(parsedProgram);
		SortedSet<Atom> onInstancesInAnswerSet = answerSet.getPredicateInstances(on);
		for (Atom atom : parsedProgram.getFacts()) {
			if (atom.getPredicate().getName().equals(ongoal.getName()) && atom.getPredicate().getArity() == ongoal.getArity()) {
				Term expectedTop = atom.getTerms().get(0);
				Term expectedBottom = atom.getTerms().get(1);
				Term expectedSteps = ConstantTerm.getInstance(steps);
				Atom expectedAtom = new BasicAtom(on, expectedSteps, expectedBottom, expectedTop);
				assertTrue(onInstancesInAnswerSet.contains(expectedAtom), "Answer set does not contain " + expectedAtom);
			}
		}
	}

	private int getSteps(InputProgram parsedProgram) {
		Predicate steps = Predicate.getInstance("steps", 1);
		for (Atom atom : parsedProgram.getFacts()) {
			if (atom.getPredicate().getName().equals(steps.getName()) && atom.getPredicate().getArity() == steps.getArity()) {
				return Integer.valueOf(atom.getTerms().get(0).toString());
			}
		}
		throw new IllegalArgumentException("No steps atom found in input program.");
	}

}
