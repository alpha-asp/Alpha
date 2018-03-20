/**
 * Copyright (c) 2018 Siemens AG
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
package at.ac.tuwien.kr.alpha.common.heuristics;

import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link NonGroundDomainSpecificHeuristicValues#isGround()}
 */
public class HeuristicValuesIsGroundTest {
	private final ProgramParser parser = new ProgramParser();
	
	@Test
	public void testGround() {
		NonGroundDomainSpecificHeuristicValues heuristicValues = getHeuristicAnnotation("x :- not y. [2@3 : z]");
		assertTrue("Not ground: " + heuristicValues, heuristicValues.isGround());
	}
	
	@Test
	public void testWeightNotGround() {
		NonGroundDomainSpecificHeuristicValues heuristicValues = getHeuristicAnnotation("x(W) :- w(W), not y. [W@3 : z]");
		assertFalse("Should not be ground: " + heuristicValues, heuristicValues.isGround());
	}
	
	@Test
	public void testLevelNotGround() {
		NonGroundDomainSpecificHeuristicValues heuristicValues = getHeuristicAnnotation("x(L) :- l(L), not y. [2@L : z]");
		assertFalse("Should not be ground: " + heuristicValues, heuristicValues.isGround());
	}
	
	@Test
	public void testGeneratorNotGround() {
		NonGroundDomainSpecificHeuristicValues heuristicValues = getHeuristicAnnotation("x :- g(G), not y. [2@3 : z(G)]");
		assertFalse("Should not be ground: " + heuristicValues, heuristicValues.isGround());
	}

	private NonGroundDomainSpecificHeuristicValues getHeuristicAnnotation(String program) {
		return parser.parse(program).getRules().stream().
				filter(r -> r.getHeuristic() != null).map(Rule::getHeuristic).findFirst().get();
	}

}
