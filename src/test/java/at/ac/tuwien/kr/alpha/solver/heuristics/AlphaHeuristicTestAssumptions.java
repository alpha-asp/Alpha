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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.MainTest.stream;
import static org.junit.Assert.assertEquals;

/**
 * Tests assumptions made by {@link AlphaHeuristic}. Even if these test cases do not test {@link AlphaHeuristic} directly, it will break if these test cases
 * break.
 * 
 * Copyright (c) 2017 Siemens AG
 *
 */
public class AlphaHeuristicTestAssumptions {

	private Grounder grounder;
	
	@Before
	public void setUp() throws IOException {
		String testProgram = "h :- b1, b2, not b3, not b4.";
		ParsedProgram parsedProgram = parseVisit(stream(testProgram));
		this.grounder = new NaiveGrounder(parsedProgram);
	}

	@Test
	public void testNumbersOfNoGoods() {
		int n = 0, bodyNotHead = 0, bodyElementsNotBody = 0, noHead = 0, other = 0;
		for (NoGood noGood : getNoGoods()) {
			n++;
			boolean knownType = false;
			if (noGood.isBodyNotHead(grounder::isAtomChoicePoint)) {
				bodyNotHead++;
				knownType = true;
			}
			if (noGood.isBodyElementsNotBody(grounder::isAtomChoicePoint)) {
				bodyElementsNotBody++;
				knownType = true;
			}
			if (!noGood.hasHead()) {
				noHead++;
				knownType = true;
			}
			if (!knownType) {
				other++;
			}
		}

		assertEquals("Unexpected number of bodyNotHead nogoods", 1, bodyNotHead);
		assertEquals("Unexpected number of bodyElementsNotBody nogoods", 1, bodyElementsNotBody);
		assertEquals("Unexpected number of nogoods without head", 4, noHead);

		// there may be other nogoods (e.g. for ChoiceOn, ChoiceOff) which we do not care for here
		System.out.println("Total number of NoGoods: " + n);
		System.out.println("Number of NoGoods of unknown type: " + other);
	}

	private Collection<NoGood> getNoGoods() {
		return grounder.getNoGoods().values();
	}
}
