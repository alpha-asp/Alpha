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
package at.ac.tuwien.kr.alpha.regressiontests;

import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.buildSolverForRegressionTest;
import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.ignoreTestForNaiveSolver;
import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.ignoreTestForNonDefaultDomainIndependentHeuristics;
import static at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTestUtils.runWithTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.SortedSet;

import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.regressiontests.util.RegressionTest;

/**
 * Tests {@link AbstractSolver} using some hanoi tower test cases (see https://en.wikipedia.org/wiki/Tower_of_Hanoi).
 *
 */
// TODO This is a functional test and should not be run with standard unit tests
public class HanoiTowerTest {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(HanoiTowerTest.class);

	private static final int DEBUG_TIMEOUT_FACTOR = 5;

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance1(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(1, cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance2(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(2, cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance3(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(3, cfg));
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testInstance4(SystemConfig cfg) {
		long timeout = 10000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower(4, cfg));
	}

	@RegressionTest
	public void testSimple(SystemConfig cfg) {
		ignoreTestForNaiveSolver(cfg);
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		long timeout = 60000L;
		runWithTimeout(cfg, timeout, DEBUG_TIMEOUT_FACTOR, () -> testHanoiTower("simple", cfg));
	}

	private void testHanoiTower(int instance, SystemConfig cfg) throws IOException {
		testHanoiTower(String.valueOf(instance), cfg);
	}

	private void testHanoiTower(String instance, SystemConfig cfg) throws IOException {
		// TODO should be read by the Alpha instance constructed in buildSolverForRegressionTest,
		// do not instantiate parsers "free-style"!
		InputProgram prog = new ProgramParserImpl().parse(
				Paths.get("src", "test", "resources", "HanoiTower_Alpha.asp"),
				Paths.get("src", "test", "resources", "HanoiTower_instances", instance + ".asp"));
		Solver solver = buildSolverForRegressionTest(prog, cfg);
		Optional<AnswerSet> answerSet = solver.stream().findFirst();
		assertTrue(answerSet.isPresent());
		checkGoal(prog, answerSet.get());
	}

	/**
	 * Conducts a very simple, non-comprehensive goal check (i.e. it may classify answer sets as correct that are actually wrong) by checking if
	 * for every goal/3
	 * fact in the input there is a corresponding on/3 atom in the output.
	 */
	private void checkGoal(InputProgram parsedProgram, AnswerSet answerSet) {
		Predicate ongoal = Predicates.getPredicate("ongoal", 2);
		Predicate on = Predicates.getPredicate("on", 3);
		int steps = getSteps(parsedProgram);
		SortedSet<Atom> onInstancesInAnswerSet = answerSet.getPredicateInstances(on);
		for (Atom atom : parsedProgram.getFacts()) {
			if (atom.getPredicate().getName().equals(ongoal.getName()) && atom.getPredicate().getArity() == ongoal.getArity()) {
				Term expectedTop = atom.getTerms().get(0);
				Term expectedBottom = atom.getTerms().get(1);
				Term expectedSteps = Terms.newConstant(steps);
				Atom expectedAtom = Atoms.newBasicAtom(on, expectedSteps, expectedBottom, expectedTop);
				assertTrue(onInstancesInAnswerSet.contains(expectedAtom), "Answer set does not contain " + expectedAtom);
			}
		}
	}

	private int getSteps(InputProgram parsedProgram) {
		Predicate steps = Predicates.getPredicate("steps", 1);
		for (Atom atom : parsedProgram.getFacts()) {
			if (atom.getPredicate().getName().equals(steps.getName()) && atom.getPredicate().getArity() == steps.getArity()) {
				return Integer.valueOf(atom.getTerms().get(0).toString());
			}
		}
		throw new IllegalArgumentException("No steps atom found in input program.");
	}

}
