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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTimeout;

public class ThreeColouringRandomGraphTest extends AbstractSolverTests {
	@Tag("slow")
	@ParameterizedTest
	@CsvSource({
		"3, 3, 1",

		"10, 18, 10",
		"20, 38, 10",
		"30, 48, 10",

		"200, 300, 60",
		"300, 200, 60",
		"300, 300, 60"
	})
	void threeColouring(int v, int e, int seconds) throws IOException {
		assertTimeout(Duration.ofSeconds(seconds), () -> {
			Program program = new ProgramParser().parse(
					"blue(N) :- v(N), not red(N), not green(N)." +
					"red(N) :- v(N), not blue(N), not green(N)." +
					"green(N) :- v(N), not red(N), not blue(N)." +
					":- e(N1,N2), blue(N1), blue(N2)." +
					":- e(N1,N2), red(N1), red(N2)." +
					":- e(N1,N2), green(N1), green(N2).");

			program.getFacts().addAll(createVertices(v));
			program.getFacts().addAll(createEdges(v, e));

			maybeShuffle(program);

			Optional<AnswerSet> answerSet = getInstance(program).stream().findAny();
			System.out.println(answerSet);

			// TODO: check correctness of answer set
		});
	}

	private void maybeShuffle(Program program) {
		// TODO: switch on if different rule orderings in the encoding are desired (e.g. for benchmarking purposes)
		// Collections.reverse(program.getRules());
		// Collections.shuffle(program.getRules());
		// Collections.reverse(program.getFacts());
		// Collections.shuffle(program.getFacts());
	}

	private Collection<Atom> createVertices(int n) {
		Collection<Atom> facts = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			facts.add(fact("v", i));
		}
		return facts;
	}

	private Collection<Atom> createEdges(int vertices, int edges) {
		Random rand = new Random(0);
		Collection<Atom> facts = new LinkedHashSet<>(edges);
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

	private Atom fact(String predicateName, int... iTerms) {
		List<Term> terms = new ArrayList<>(1);
		for (int i : iTerms) {
			terms.add(ConstantTerm.getInstance(i));
		}
		return new BasicAtom(new Predicate(predicateName, iTerms.length), terms);
	}
}
