/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.ArrayAssignment;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DomainSpecific} heuristics.
 */
public class DomainSpecificHeuristicsTest {
	
	private final WritableAssignment assignment = new ArrayAssignment();
	private final ProgramParser parser = new ProgramParser();
	
	@Before
	public void setUp() {
		assignment.growForMaxAtomId(2);
	}
	
	@Test
	public void testSimpleHeuristicProgram_AB() {
		Program program = parser.parse(
				"a :- not b, not _h(2,1)." + System.lineSeparator() +
				"b :- not a, not _h(1,1).");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}

	@Test
	public void testSimpleHeuristicProgram_BA() {
		Program program = parser.parse(
				"a :- not b, not _h(1,1)." + System.lineSeparator() +
				"b :- not a, not _h(2,1).");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}

	private void solveAndAssertAnswerSets(Program program, String... expectedAnswerSets) {
		Solver solver = SolverFactory.getInstance("default", "alpharoaming", GrounderFactory.getInstance("naive", program), new Random(), Heuristic.DOMAIN, true);
		List<AnswerSet> listOfAnswerSets = solver.collectList();
		assertEquals(expectedAnswerSets.length, listOfAnswerSets.size());
		for (int i = 0; i < expectedAnswerSets.length; i++) {
			assertEquals(expectedAnswerSets[i], listOfAnswerSets.get(i).toString());
		}
	}
}
