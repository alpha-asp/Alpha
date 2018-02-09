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
	public void testSimpleHeuristicProgram_HeuristicAtom_AB_SameLevel() {
		Program program = parser.parse(
				"a :- not b, not _h(2,1)." + System.lineSeparator() +
				"b :- not a, not _h(1,1).");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}
	
	@Test
	public void testSimpleHeuristicProgram_HeuristicAtom_AB_DominatingLevel() {
		Program program = parser.parse(
				"a :- not b, not _h(1,2)." + System.lineSeparator() +
				"b :- not a, not _h(2,1).");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}

	@Test
	public void testSimpleHeuristicProgram_HeuristicAtom_BA_SameLevel() {
		Program program = parser.parse(
				"a :- not b, not _h(1,1)." + System.lineSeparator() +
				"b :- not a, not _h(2,1).");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}

	@Test
	public void testSimpleHeuristicProgram_HeuristicAtom_BA_DominatingLevel() {
		Program program = parser.parse(
				"a :- not b, not _h(2,1)." + System.lineSeparator() +
				"b :- not a, not _h(1,2).");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}
	
	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_AB_SameLevel() {
		Program program = parser.parse(
				"a :- not b. [2@1]" + System.lineSeparator() +
				"b :- not a. [1@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}
	
	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_AB_DominatingLevel() {
		Program program = parser.parse(
				"a :- not b. [1@2]" + System.lineSeparator() +
				"b :- not a. [2@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}

	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_BA_SameLevel() {
		Program program = parser.parse(
				"a :- not b. [1@1]" + System.lineSeparator() +
				"b :- not a. [2@1]");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}

	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_BA_DominatingLevel() {
		Program program = parser.parse(
				"a :- not b. [2@1]" + System.lineSeparator() +
				"b :- not a. [1@2]");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}

	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_SimpleChoiceGenerator_NegativeLiteral() {
		Program program = parser.parse(
				"c :- not nc. [10@1]" + System.lineSeparator() +
				"nc :- not c." + System.lineSeparator() +
				"a :- not b. [1@1 : c]" + System.lineSeparator() +
				"b :- not a. [1@1 : not c]");
		solveAndAssertAnswerSets(program, "{ a, c }", "{ b, c }", "{ b, nc }", "{a, nc}");
	}

	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_SimpleChoiceGenerator_PositiveLiteral() {
		Program program = parser.parse(
				"c :- not nc. [10@1]" + System.lineSeparator() +
				"nc :- not c." + System.lineSeparator() +
				"a :- not b. [1@1 : c]" + System.lineSeparator() +
				"b :- not a. [1@1 : not c]");
		solveAndAssertAnswerSets(program, "{ a, c }", "{ b, c }", "{ b, nc }", "{a, nc}");
	}
	
	@Test
	public void testSimpleHeuristicProgram_HeuristicAnnotation_SimpleNonGroundArithmetics() {
		Program program = parser.parse(
				"n(2)." + System.lineSeparator() +
				"a :- n(N), not b. [Np1@1 : Np1=N+1]" + System.lineSeparator() +
				"b :- not a. [1]");
		solveAndAssertAnswerSets(program, "{ a, n(2) }", "{ b, n(2) }");
	}

	private void solveAndAssertAnswerSets(Program program, String... expectedAnswerSets) {
		Solver solver = SolverFactory.getInstance("default", "alpharoaming", GrounderFactory.getInstance("naive", program), new Random(), true, Heuristic.NAIVE, true);
		List<AnswerSet> listOfAnswerSets = solver.collectList();
		assertEquals(expectedAnswerSets.length, listOfAnswerSets.size());
		for (int i = 0; i < expectedAnswerSets.length; i++) {
			assertEquals(expectedAnswerSets[i], listOfAnswerSets.get(i).toString());
		}
	}
}
