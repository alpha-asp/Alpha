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
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Tests {@link AbstractSolver} using some three-coloring test cases, as described in:
 * Lefèvre, Claire; Béatrix, Christopher; Stéphan, Igor; Garcia, Laurent (2017):
 * ASPeRiX, a first-order forward chaining approach for answer set computing.
 * In Theory and Practice of Logic Programming, pp. 1-45.
 * DOI: 10.1017/S1471068416000569
 */
public class ThreeColouringTestWithRandom extends AbstractSolverTests {
	@Test(timeout = 3000)
	public void testN3() throws IOException {
		testThreeColouring(3, false, 0);
	}

	@Test(timeout = 4000)
	public void testN4() throws IOException {
		testThreeColouring(4, false, 0);
	}

	@Test(timeout = 5000)
	@Ignore("disabled to save resources during CI")
	public void testN5() throws IOException {
		testThreeColouring(5, false, 0);
	}

	@Test(timeout = 6000)
	@Ignore("disabled to save resources during CI")
	public void testN6() throws IOException {
		testThreeColouring(6, false, 0);
	}

	@Test(timeout = 7000)
	@Ignore("disabled to save resources during CI")
	public void testN7() throws IOException {
		testThreeColouring(7, false, 0);
	}

	@Test(timeout = 8000)
	@Ignore("disabled to save resources during CI")
	public void testN8() throws IOException {
		testThreeColouring(8, false, 0);
	}

	@Test(timeout = 9000)
	@Ignore("disabled to save resources during CI")
	public void testN9() throws IOException {
		testThreeColouring(9, false, 0);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testN10() throws IOException {
		testThreeColouring(10, false, 0);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testN10Random0() throws IOException {
		testThreeColouring(10, true, 0);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testN10Random1() throws IOException {
		testThreeColouring(10, true, 1);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testN10Random2() throws IOException {
		testThreeColouring(10, true, 2);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testN10Random3() throws IOException {
		testThreeColouring(10, true, 3);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN19() throws IOException {
		testThreeColouring(19, false, 0);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN19Random0() throws IOException {
		testThreeColouring(19, true, 0);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN19Random1() throws IOException {
		testThreeColouring(19, true, 1);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN19Random2() throws IOException {
		testThreeColouring(19, true, 2);
	}

	@Test(timeout = 60000)
	@Ignore("disabled to save resources during CI")
	public void testN19Random3() throws IOException {
		testThreeColouring(19, true, 3);
	}

	@Test(timeout = 10000)
	@Ignore("disabled to save resources during CI")
	public void testN101() throws IOException {
		testThreeColouring(101, false, 0);
	}

	private void testThreeColouring(int n, boolean shuffle, int seed) throws IOException {
		Program program = new ProgramParser().parse("col(V,C) :- v(V), c(C), not ncol(V,C)." + "ncol(V,C) :- col(V,D), c(C), C != D." + ":- e(V,U), col(V,C), col(U,C).");
		program.getFacts().addAll(createColors("1", "2", "3"));
		program.getFacts().addAll(createVertices(n));
		program.getFacts().addAll(createEdges(n, shuffle, seed));

		Solver solver = getInstance(program);

		Optional<AnswerSet> answerSet = solver.stream().findAny();
		//System.out.println(answerSet);
		// TODO: check correctness of answer set
	}

	private List<Atom> createColors(String... colours) {
		List<Atom> facts = new ArrayList<>(colours.length);
		for (String colour : colours) {
			List<Term> terms = new ArrayList<>(1);
			terms.add(ConstantTerm.getInstance(colour));
			facts.add(new BasicAtom(new Predicate("c", 1), terms));
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
	 * @param shuffle
	 *          if true, the vertex indices are shuffled with the given seed
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
		Predicate predicate = new Predicate(predicateName, iTerms.length);
		for (int i : iTerms) {
			terms.add(ConstantTerm.getInstance(i));
		}
		return new BasicAtom(predicate, terms);
	}
}