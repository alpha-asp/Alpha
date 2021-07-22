/**
 * Copyright (c) 2017 Siemens AG
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

import static at.ac.tuwien.kr.alpha.test.util.TestUtils.collectRegressionTestAnswerSets;
import static at.ac.tuwien.kr.alpha.test.util.TestUtils.ignoreTestForNonDefaultDomainIndependentHeuristics;
import static at.ac.tuwien.kr.alpha.test.util.TestUtils.runWithTimeout;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;

import at.ac.tuwien.kr.alpha.common.AnswerSet;

/**
 * Tests {@link AbstractSolver} using some pigeon-hole test cases (see https://en.wikipedia.org/wiki/Pigeonhole_principle).
 */
public class PigeonHoleTest {
	
	private static final long DEBUG_TIMEOUT_FACTOR = 5;
	
	@RegressionTest
	public void test2Pigeons2Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 5000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(2, 2, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void test3Pigeons2Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 5000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(3, 2, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void test2Pigeons3Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 5000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(2, 3, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void test3Pigeons3Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(3, 3, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void test4Pigeons3Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(4, 3, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void test3Pigeons4Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(3, 4, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void test4Pigeons4Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		ignoreTestForNonDefaultDomainIndependentHeuristics(cfg);
		runWithTimeout(() -> testPigeonsHoles(4, 4, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void test10Pigeons10Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testPigeonsHoles(10, 10, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void test19Pigeons20Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testPigeonsHoles(19, 20, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void test28Pigeons30Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testPigeonsHoles(28, 30, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void test37Pigeons40Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testPigeonsHoles(37, 40, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void test46Pigeons50Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testPigeonsHoles(46, 50, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void test55Pigeons60Holes(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testPigeonsHoles(55, 60, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	/**
	 * Tries to solve the problem of assigning P pigeons to H holes.
	 */
	private void testPigeonsHoles(int pigeons, int holes, RegressionTestConfig cfg) throws IOException {
		List<String> rules = new ArrayList<>();
		rules.add("pos(P,H) :- pigeon(P), hole(H), not negpos(P,H).");
		rules.add("negpos(P,H) :- pigeon(P), hole(H), not pos(P,H).");
		rules.add(":- pigeon(P), hole(H1), hole(H2), pos(P,H1), pos(P,H2), H1 != H2.");
		rules.add(":- pigeon(P), not hashole(P).");
		rules.add("hashole(P) :- pigeon(P), hole(H), pos(P,H).");
		rules.add(":- pigeon(P1), pigeon(P2), hole(H), pos(P1,H), pos(P2,H), P1 != P2.");

		addPigeons(rules, pigeons);
		addHoles(rules, holes);

		Set<AnswerSet> answerSets = collectRegressionTestAnswerSets(concat(rules), cfg);
		assertEquals(numberOfSolutions(pigeons, holes), answerSets.size());
	}

	private void addPigeons(List<String> rules, int pigeons) {
		addFacts(rules, "pigeon", 1, pigeons);
	}

	private void addHoles(List<String> rules, int holes) {
		addFacts(rules, "hole", 1, holes);
	}

	private void addFacts(List<String> rules, String predicateName, int from, int to) {
		for (int i = from; i <= to; i++) {
			rules.add(String.format("%s(%d).", predicateName, i));
		}
	}

	private String concat(List<String> rules) {
		String ls = System.lineSeparator();
		return rules.stream().collect(Collectors.joining(ls));
	}

	private long numberOfSolutions(int pigeons, int holes) {
		if (pigeons > holes) {
			return 0;
		} else if (pigeons == holes) {
			return factorial(pigeons);
		} else {
			return factorial(holes) / factorial(holes - pigeons);
			// could be replaced by more efficient implementaton (but performance is not so important here)
		}
	}

	private long factorial(int n) {
		return n <= 1 ? 1 : n * factorial(n - 1);
		// could be replaced by more efficient implementaton (but performance is not so important here)
		// see http://www.luschny.de/math/factorial/FastFactorialFunctions.htm
		// TODO: we could use Apache Commons Math
	}

}
