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
import at.ac.tuwien.kr.alpha.grounder.parser.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;

public class ThreeColouringRandomGraphTest extends AbstractSolverTests {
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
		System.out.println(solverName);
	}

	@Test(timeout = 1000)
	public void testV3E3() throws IOException {
		testThreeColouring(3, 3);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testV10E18() throws IOException {
		testThreeColouring(10, 18);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testV20E38() throws IOException {
		testThreeColouring(20, 38);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testV30E48() throws IOException {
		testThreeColouring(30, 48);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testV200E300() throws IOException {
		testThreeColouring(200, 300);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testV300E200() throws IOException {
		testThreeColouring(300, 200);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testV300E300() throws IOException {
		testThreeColouring(300, 300);
	}

	private void testThreeColouring(int nVertices, int nEdges) throws IOException {
		ParsedProgram program = parseVisit(
				"blue(N) :- v(N), not red(N), not green(N)." +
				"red(N) :- v(N), not blue(N), not green(N)." +
				"green(N) :- v(N), not red(N), not blue(N)." +
				":- e(N1,N2), blue(N1), blue(N2)." +
				":- e(N1,N2), red(N1), red(N2)." +
				":- e(N1,N2), green(N1), green(N2).");
		Collection<ParsedFact> vertices = createVertices(nVertices);
		program.accumulate(new ParsedProgram(vertices));
		Collection<ParsedFact> edges = createEdges(nVertices, nEdges);
		program.accumulate(new ParsedProgram(edges));

		program = maybeShuffle(program);

		NaiveGrounder grounder = new NaiveGrounder(program);
		Solver solver = getInstance(grounder);

		Optional<AnswerSet> answerSet = solver.stream().findAny();
		System.out.println(answerSet);

		// TODO: check correctness of answer set
	}

	private ParsedProgram maybeShuffle(ParsedProgram program) {
		List<CommonParsedObject> rules = new ArrayList<>();
		rules.addAll(program.facts);
		rules.addAll(program.rules);
		rules.addAll(program.constraints);

		// TODO: switch on if different rule orderings in the encoding are desired (e.g. for benchmarking purposes)
		// Collections.reverse(rules);
		// Collections.shuffle(rules);

		return new ParsedProgram(rules);
	}

	private Collection<ParsedFact> createVertices(int n) {
		Collection<ParsedFact> facts = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			facts.add(fact("v", i));
		}
		return facts;
	}

	private Collection<ParsedFact> createEdges(int vertices, int edges) {
		Random rand = new Random(0);
		Collection<ParsedFact> facts = new LinkedHashSet<>(edges);
		for (int i = 0; i < edges; i++) {
			int v1 = 0;
			int v2 = 0;
			while (v1 == v2) {
				v1 = rand.nextInt(vertices);
				v2 = rand.nextInt(vertices);
			}
			facts.add(fact("e", v1, v2));
			facts.add(fact("e", v2, v1));
		}
		return facts;
	}

	private ParsedFact fact(String predicateName, int... iTerms) {
		List<ParsedTerm> terms = new ArrayList<>(1);
		for (int i : iTerms) {
			terms.add(new ParsedConstant(i2s(i), ParsedConstant.Type.NUMBER));
		}
		return new ParsedFact(new ParsedAtom(predicateName, terms));
	}

	private String i2s(int i) {
		return String.valueOf(i).intern();
	}

}
