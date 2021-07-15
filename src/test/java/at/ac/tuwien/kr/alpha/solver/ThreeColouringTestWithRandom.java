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

import static at.ac.tuwien.kr.alpha.test.util.TestUtils.buildSolverForRegressionTest;
import static at.ac.tuwien.kr.alpha.test.util.TestUtils.runWithTimeout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Tests {@link AbstractSolver} using some three-coloring test cases, as described in: 
 * Lefèvre, Claire; Béatrix, Christopher; Stéphan, Igor; Garcia, Laurent (2017): 
 * ASPeRiX, a first-order forward chaining approach for answer set computing. 
 * In Theory and Practice of Logic Programming, pp. 1-45. DOI:
 * 10.1017/S1471068416000569
 */
public class ThreeColouringTestWithRandom {
	
	private static final long DEBUG_TIMEOUT_FACTOR = 5;
	
	@RegressionTest
	public void testN3(RegressionTestConfig cfg) throws IOException {
		long timeout = 3000L;
		runWithTimeout(() -> testThreeColouring(3, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	public void testN4(RegressionTestConfig cfg) throws IOException {
		long timeout = 4000L;
		runWithTimeout(() -> testThreeColouring(4, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN5(RegressionTestConfig cfg) throws IOException {
		long timeout = 5000L;
		runWithTimeout(() -> testThreeColouring(5, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN6(RegressionTestConfig cfg) throws IOException {
		long timeout = 6000L;
		runWithTimeout(() -> testThreeColouring(6, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN7(RegressionTestConfig cfg) throws IOException {
		long timeout = 7000L;
		runWithTimeout(() -> testThreeColouring(7, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Ignore("disabled to save resources during CI")
	public void testN8(RegressionTestConfig cfg) throws IOException {
		long timeout = 8000L;
		runWithTimeout(() -> testThreeColouring(8, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN9(RegressionTestConfig cfg) throws IOException {
		long timeout = 9000L;
		runWithTimeout(() -> testThreeColouring(9, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN10(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		runWithTimeout(() -> testThreeColouring(10, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN10Random0(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		runWithTimeout(() -> testThreeColouring(10, true, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN10Random1(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		runWithTimeout(() -> testThreeColouring(10, true, 1, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN10Random2(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		runWithTimeout(() -> testThreeColouring(10, true, 2, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN10Random3(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		runWithTimeout(() -> testThreeColouring(10, true, 3, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN19(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testThreeColouring(19, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN19Random0(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testThreeColouring(19, true, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN19Random1(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testThreeColouring(19, true, 1, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN19Random2(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testThreeColouring(19, true, 2, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN19Random3(RegressionTestConfig cfg) throws IOException {
		long timeout = 60000L;
		runWithTimeout(() -> testThreeColouring(19, true, 3, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	@RegressionTest
	@Disabled("disabled to save resources during CI")
	public void testN101(RegressionTestConfig cfg) throws IOException {
		long timeout = 10000L;
		runWithTimeout(() -> testThreeColouring(101, false, 0, cfg), cfg.isDebugChecks() ? timeout * DEBUG_TIMEOUT_FACTOR : timeout);
	}

	private void testThreeColouring(int n, boolean shuffle, int seed, RegressionTestConfig cfg) throws IOException {
		InputProgram tmpPrg = new ProgramParser()
				.parse("col(V,C) :- v(V), c(C), not ncol(V,C)." + "ncol(V,C) :- col(V,D), c(C), C != D." + ":- e(V,U), col(V,C), col(U,C).");
		InputProgram.Builder prgBuilder = InputProgram.builder().accumulate(tmpPrg);
		prgBuilder.addFacts(createColors("1", "2", "3"));
		prgBuilder.addFacts(createVertices(n));
		prgBuilder.addFacts(createEdges(n, shuffle, seed));
		InputProgram program = prgBuilder.build();

		Solver solver = buildSolverForRegressionTest(program, cfg);
		Optional<AnswerSet> answerSet = solver.stream().findAny();
		// System.out.println(answerSet);
		// TODO: check correctness of answer set
	}

	private List<Atom> createColors(String... colours) {
		List<Atom> facts = new ArrayList<>(colours.length);
		for (String colour : colours) {
			List<Term> terms = new ArrayList<>(1);
			terms.add(ConstantTerm.getInstance(colour));
			facts.add(new BasicAtom(Predicate.getInstance("c", 1), terms));
		}
		return facts;
	}

	private List<Atom> createVertices(int n) {
		List<Atom> facts = new ArrayList<>(n);
		for (int i = 1; i <= n; i++) {
			facts.add(fact("v", i));
		}
		return facts;
	}

	/**
	 * 
	 * @param n
	 * @param shuffle if true, the vertex indices are shuffled with the given seed
	 * @param seed
	 * @return
	 */
	private List<Atom> createEdges(int n, boolean shuffle, int seed) {
		List<Atom> facts = new ArrayList<>(n);
		List<Integer> indices = new ArrayList<>();
		for (int i = 1; i <= n; i++) {
			indices.add(i);
		}
		if (shuffle) {
			Collections.shuffle(indices, new Random(seed));
		}

		for (int i = 1; i < n; i++) {
			facts.add(fact("e", indices.get(0), indices.get(i)));
		}
		for (int i = 1; i < n - 1; i++) {
			facts.add(fact("e", indices.get(i), indices.get(i + 1)));
		}
		facts.add(fact("e", indices.get(1), indices.get(n - 1)));
		return facts;
	}

	private Atom fact(String predicateName, int... iTerms) {
		List<Term> terms = new ArrayList<>(iTerms.length);
		Predicate predicate = Predicate.getInstance(predicateName, iTerms.length);
		for (int i : iTerms) {
			terms.add(ConstantTerm.getInstance(i));
		}
		return new BasicAtom(predicate, terms);
	}
}