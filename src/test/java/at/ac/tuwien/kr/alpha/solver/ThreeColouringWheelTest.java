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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;

/**
 * Tests {@link AbstractSolver} using some three-coloring test cases, as described in:
 * Lefèvre, Claire; Béatrix, Christopher; Stéphan, Igor; Garcia, Laurent (2017):
 * ASPeRiX, a first-order forward chaining approach for answer set computing.
 * In Theory and Practice of Logic Programming, pp. 1-45.
 * DOI: 10.1017/S1471068416000569
 */
public class ThreeColouringWheelTest extends AbstractSolverTests {
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
	public void testN4() throws IOException {
		testThreeColouring(4);
	}

	@Test(timeout = 1000)
	public void testN5() throws IOException {
		testThreeColouring(5);
	}

	@Test(timeout = 6000)
	@Ignore("disabled to save resources during CI")
	public void testN6() throws IOException {
		testThreeColouring(6);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN3() throws IOException {
		testThreeColouring(3);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN7() throws IOException {
		testThreeColouring(7);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN11() throws IOException {
		testThreeColouring(11);
	}

	private void testThreeColouring(int n) throws IOException {
		ParsedProgram parsedProgram = parseVisit(
				"col(V,C) :- v(V), c(C), not ncol(V,C)." +
				"ncol(V,C) :- col(V,D), c(C), C != D." +
				":- e(V,U), col(V,C), col(U,C).");
		Collection<ParsedFact> colours = createColors("red", "blue", "green");
		parsedProgram.accumulate(new ParsedProgram(colours));
		Collection<ParsedFact> vertices = createVertices(n);
		parsedProgram.accumulate(new ParsedProgram(vertices));
		Collection<ParsedFact> edges = createEdges(n);
		parsedProgram.accumulate(new ParsedProgram(edges));

		parsedProgram = maybeShuffle(parsedProgram);

		NaiveGrounder grounder = new NaiveGrounder(parsedProgram.toProgram());
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
		return new ParsedProgram(rules);
	}

	private Collection<ParsedFact> createColors(String... colours) {
		Collection<ParsedFact> facts = new ArrayList<>(colours.length);
		for (String colour : colours) {
			List<ParsedTerm> terms = new ArrayList<>(1);
			terms.add(new ParsedConstant<>(colour));
			facts.add(new ParsedFact(new ParsedAtom("c", terms)));
		}
		return facts;
	}

	private Collection<ParsedFact> createVertices(int n) {
		Collection<ParsedFact> facts = new ArrayList<>(n);
		for (int i = 1; i <= n; i++) {
			facts.add(fact("v", i));
		}
		return facts;
	}

	private Collection<ParsedFact> createEdges(int n) {
		Collection<ParsedFact> facts = new ArrayList<>(n);
		for (int i = 2; i <= n; i++) {
			facts.add(fact("e", 1, i));
		}
		for (int i = 2; i <= n - 1; i++) {
			facts.add(fact("e", i, i + 1));
		}
		facts.add(fact("e", n, 2));
		return facts;
	}

	private ParsedFact fact(String predicateName, int... iTerms) {
		List<ParsedTerm> terms = new ArrayList<>(1);
		for (int i : iTerms) {
			terms.add(new ParsedConstant<>(i));
		}
		return new ParsedFact(new ParsedAtom(predicateName, terms));
	}
}
