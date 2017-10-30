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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 * Tests {@link AbstractSolver} using some pigeon-hole test cases (see https://en.wikipedia.org/wiki/Pigeonhole_principle).
 *
 */
public class PigeonHoleTest extends AbstractSolverTests {
	@ParameterizedTest
	@CsvSource({
		"2, 2",
		"3, 2",
		"2, 3",
		"3, 3",
		"4, 3",
		"3, 4",
		"4, 4",
	})
	public void pigeonHolesSmall(int pigeons, int holes) {
		assertTimeout(Duration.ofSeconds(1), () ->
			testPigeonsHoles(pigeons, holes)
		);
	}

	@Tag("slow")
	@ParameterizedTest
	@CsvSource({
		"10, 10",
		"19, 20",
		"28, 30",
		"37, 40",
		"46, 50",
		"55, 60"
	})
	public void pigeonHolesLarge(int pigeons, int holes) {
		assertTimeout(Duration.ofMinutes(1), () ->
			testPigeonsHoles(pigeons, holes)
		);
	}

	/**
	 * Tries to solve the problem of assigning P pigeons to H holes.
	 */
	private void testPigeonsHoles(int pigeons, int holes) throws IOException {
		List<String> rules = new ArrayList<>();
		rules.add("pos(P,H) :- pigeon(P), hole(H), not negpos(P,H).");
		rules.add("negpos(P,H) :- pigeon(P), hole(H), not pos(P,H).");
		rules.add(":- pigeon(P), hole(H1), hole(H2), pos(P,H1), pos(P,H2), H1 != H2.");
		rules.add(":- pigeon(P), not hashole(P).");
		rules.add("hashole(P) :- pigeon(P), hole(H), pos(P,H).");
		rules.add(":- pigeon(P1), pigeon(P2), hole(H), pos(P1,H), pos(P2,H), P1 != P2.");

		addFacts(rules, "pigeon", 1, pigeons);
		addFacts(rules, "hole", 1, holes);

		Set<AnswerSet> answerSets = collectSet(concat(rules));
		assertEquals(numberOfSolutions(pigeons, holes), answerSets.size());
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
