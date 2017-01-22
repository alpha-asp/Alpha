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

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link AbstractSolver} using some pigeon-hole test cases (see https://en.wikipedia.org/wiki/Pigeonhole_principle).
 *
 */
public class PigeonHoleTest extends AbstractSolverTests {
	/**
	 * Sets the logging level to TRACE. Useful for debugging; call at beginning of test case.
	 */
	private static void enableTracing() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(ch.qos.logback.classic.Level.TRACE);
	}

	private static void enableDebugLog() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
	}

	@Test(timeout = 1000)
	public void test2Pigeons2Holes() throws IOException {
		testPigeonsHoles(2, 2);
	}

	@Test(timeout = 1000)
	public void test3Pigeons2Holes() throws IOException {
		testPigeonsHoles(3, 2);
	}

	@Test(timeout = 1000)
	public void test2Pigeons3Holes() throws IOException {
		testPigeonsHoles(2, 3);
	}

	@Test(timeout = 5000)
	public void test3Pigeons3Holes() throws IOException {
		testPigeonsHoles(3, 3);
	}

	@Test(timeout = 10000)
	public void test4Pigeons3Holes() throws IOException {
		testPigeonsHoles(4, 3);
	}

	@Test(timeout = 10000)
	public void test3Pigeons4Holes() throws IOException {
		testPigeonsHoles(3, 4);
	}

	/**
	 * Tries to solve the problem of assigning P pigeons to H holes.
	 */
	private void testPigeonsHoles(int pigeons, int holes) throws IOException {
		String ls = System.lineSeparator();
		StringBuilder testProgram = new StringBuilder();
		testProgram.append("n(N) :- pigeon(N).").append(ls);
		testProgram.append("n(N) :- hole(N).").append(ls);
		testProgram.append("eq(N,N) :- n(N).").append(ls);
		testProgram.append("in(P,H) :- pigeon(P), hole(H), not not_in(P,H).").append(ls);
		testProgram.append("not_in(P,H) :- pigeon(P), hole(H), not in(P,H).").append(ls);
		testProgram.append(":- in(P,H1), in(P,H2), not eq(H1,H2).").append(ls);
		testProgram.append(":- in(P1,H), in(P2,H), not eq(P1,P2).").append(ls);
		testProgram.append("assigned(P) :- pigeon(P), in(P,H).").append(ls);
		testProgram.append(":- pigeon(P), not assigned(P).").append(ls);
		addPigeons(testProgram, pigeons);
		addHoles(testProgram, holes);

		ParsedProgram parsedProgram = parseVisit(testProgram.toString());
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		Solver solver = getInstance(grounder);

		Set<AnswerSet> answerSets = solver.collectSet();
		assertEquals(numberOfSolutions(pigeons, holes), answerSets.size());
	}

	private void addPigeons(StringBuilder testProgram, int pigeons) {
		addFacts(testProgram, "pigeon", 1, pigeons);
	}

	private void addHoles(StringBuilder testProgram, int holes) {
		addFacts(testProgram, "hole", 1, holes);
	}

	private void addFacts(StringBuilder testProgram, String predicateName, int from, int to) {
		String ls = System.lineSeparator();
		for (int i = from; i <= to; i++) {
			testProgram.append(String.format("%s(%d).%s", predicateName, i, ls));
		}
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
