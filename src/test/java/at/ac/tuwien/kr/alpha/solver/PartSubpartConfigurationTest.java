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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;

/**
 * Tests {@link AbstractSolver} using some configuration test cases in which subparts are assigned to parts.
 *
 */
public class PartSubpartConfigurationTest extends AbstractSolverTests {
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

	@Before
	public void printSolverName() {
		System.out.print(solverName);
	}

	@Test(timeout = 1000)
	public void testN2() throws IOException {
		testPartSubpart(2);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN4() throws IOException {
		testPartSubpart(4);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN8() throws IOException {
		testPartSubpart(8);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN16() throws IOException {
		testPartSubpart(16);
	}

	@Test(timeout = 61000)
	@Ignore("disabled to save resources during CI")
	public void testN32() throws IOException {
		testPartSubpart(32);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN60() throws IOException {
		testPartSubpart(60);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN75() throws IOException {
		testPartSubpart(75);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN100() throws IOException {
		testPartSubpart(100);
	}

	private void testPartSubpart(int n) throws IOException {
		List<String> rules = new ArrayList<>();
		for (int i = 1; i <= n; i++) {
			rules.add(String.format("n(%d).", i));
		}
		rules.add("part(N) :- n(N), not no_part(N).");
		rules.add("no_part(N) :- n(N), not part(N).");
		rules.add("subpartid(SP,ID) :- subpart(SP,P), n(ID), not no_subpartid(SP,ID).");
		rules.add("no_subpartid(SP,ID) :- subpart(SP,P), n(ID), not subpartid(SP,ID).");
		rules.add("subpart(SP,P) :- part(P), part(SP), P != SP, not no_subpart(SP,P).");
		rules.add("no_subpart(SP,P) :- part(P), part(SP), P != SP, not subpart(SP,P).");
		rules.add(":- subpart(SP,P1), subpart(SP,P2), P1 != P2.");
		rules.add(":- subpart(SP1,P), subpart(SP2, P), SP1!=SP2, subpartid(SP1,ID), subpartid(SP2,ID).");

		String testProgram = concat(rules);
		ParsedProgram parsedProgram = parseVisit(testProgram);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
		Solver solver = getInstance(grounder);

		Optional<AnswerSet> answerSet = solver.stream().findAny();
		System.out.println(answerSet);
		// TODO: check correctness of answer set
	}

	private String concat(List<String> rules) {
		String ls = System.lineSeparator();
		return rules.stream().collect(Collectors.joining(ls));
	}

}
