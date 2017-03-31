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
import org.junit.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;

/**
 * Tests {@link AbstractSolver} using some three-coloring test cases, as described in: Lefèvre, Claire; Béatrix, Christopher; Stéphan, Igor; Garcia, Laurent
 * (2017): ASPeRiX, a first-order forward chaining approach for answer set computing. In Theory and Practice of Logic Programming, pp. 1–45. DOI:
 * 10.1017/S1471068416000569
 */
@Ignore
public class ThreeColouringTestWithRandom extends AbstractSolverTests {
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

	// @Test(timeout = 3000)
	// public void testN3() throws IOException {
	// testThreeColouring(3, false, 0);
	// }
	//
	// @Test(timeout = 4000)
	// public void testN4() throws IOException {
	// testThreeColouring(4, false, 0);
	// }
	//
	// @Test(timeout = 5000)
	// public void testN5() throws IOException {
	// testThreeColouring(5, false, 0);
	// }
	//
	// @Test(timeout = 6000)
	// public void testN6() throws IOException {
	// testThreeColouring(6, false, 0);
	// }
	//
	// @Test(timeout = 7000)
	// public void testN7() throws IOException {
	// testThreeColouring(7, false, 0);
	// }
	//
	// @Test(timeout = 8000)
	// public void testN8() throws IOException {
	// testThreeColouring(8, false, 0);
	// }
	//
	// @Test(timeout = 9000)
	// public void testN9() throws IOException {
	// testThreeColouring(9, false, 0);
	// }

	@Test(timeout = 10000)
	public void testN10() throws IOException {
		testThreeColouring(10, false, 0);
	}

	@Test(timeout = 10000)
	public void testN10Random0() throws IOException {
		testThreeColouring(10, true, 0);
	}

	@Test(timeout = 10000)
	public void testN10Random1() throws IOException {
		testThreeColouring(10, true, 1);
	}

	@Test(timeout = 10000)
	public void testN10Random2() throws IOException {
		testThreeColouring(10, true, 2);
	}

	@Test(timeout = 10000)
	public void testN10Random3() throws IOException {
		testThreeColouring(10, true, 3);
	}

	@Test(timeout = 60000)
	public void testN19() throws IOException {
		testThreeColouring(19, false, 0);
	}

	@Test(timeout = 60000)
	public void testN19Random0() throws IOException {
		testThreeColouring(19, true, 0);
	}

	@Test(timeout = 60000)
	public void testN19Random1() throws IOException {
		testThreeColouring(19, true, 1);
	}

	@Test(timeout = 60000)
	public void testN19Random2() throws IOException {
		testThreeColouring(19, true, 2);
	}

	@Test(timeout = 60000)
	public void testN19Random3() throws IOException {
		testThreeColouring(19, true, 3);
	}

	// @Test(timeout = 10000)
	// public void testN101() throws IOException {
	// testThreeColouring(101, false, 0);
	// }

	private void testThreeColouring(int n, boolean shuffle, int seed) throws IOException {
		ParsedProgram program = parseVisit("col(V,C) :- v(V), c(C), not ncol(V,C)." + "ncol(V,C) :- col(V,D), c(C), C != D." + ":- e(V,U), col(V,C), col(U,C).");
		List<CommonParsedObject> colours = createColors("1", "2", "3");
		program.accumulate(new ParsedProgram(colours));
		List<CommonParsedObject> vertices = createVertices(n);
		program.accumulate(new ParsedProgram(vertices));
		List<CommonParsedObject> edges = createEdges(n, shuffle, seed);
		program.accumulate(new ParsedProgram(edges));

		NaiveGrounder grounder = new NaiveGrounder(program);
		Solver solver = getInstance(grounder);

		for (ParsedFact fact : program.facts) {
			System.out.println(fact.getFact().toString() + ".");
		}
		for (ParsedRule rule : program.rules) {
			System.out.print(rule.head.toString());
			System.out.print(":-");
			for (int i = 0; i < rule.body.size(); i++) {
				if (i > 0) {
					System.out.print(", ");
				}
				System.out.print(rule.body.get(i).toString());
			}
			System.out.println(".");
		}

		for (ParsedConstraint constraint : program.constraints) {
			System.out.print(":-");
			for (int i = 0; i < constraint.body.size(); i++) {
				if (i > 0) {
					System.out.print(", ");
				}
				System.out.print(constraint.body.get(i).toString());
			}
			System.out.println(".");
		}

		Optional<AnswerSet> answerSet = solver.stream().findAny();
		System.out.println(answerSet);
		// TODO: check correctness of answer set
	}

	private List<CommonParsedObject> createColors(String... colours) {
		List<CommonParsedObject> facts = new ArrayList<>(colours.length);
		for (String colour : colours) {
			List<ParsedTerm> terms = new ArrayList<>(1);
			terms.add(new ParsedConstant(colour, ParsedConstant.Type.STRING));
			facts.add(new ParsedFact(new ParsedAtom("c", terms)));
		}
		return facts;
	}

	private List<CommonParsedObject> createVertices(int n) {
		List<CommonParsedObject> facts = new ArrayList<>(n);
		for (int i = 1; i <= n; i++) {
			facts.add(fact("v", i));
		}
		return facts;
	}

	/**
	 * 
	 * @param n
	 * @param shuffle
	 *          if true, the vertex indices are shuffled with the given seed
	 * @param seed
	 * @return
	 */
	private List<CommonParsedObject> createEdges(int n, boolean shuffle, int seed) {
		List<CommonParsedObject> facts = new ArrayList<>(n);
		List<Integer> indices = new ArrayList<Integer>();
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