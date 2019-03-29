/**
 * Copyright (c) 2018-2019 Siemens AG
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

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DomainSpecific} heuristics.
 */
public class DomainSpecificHeuristicsTest {
	
	private static final String LS = System.lineSeparator();
	private final AtomStore atomStore;
	private final ProgramParser parser = new ProgramParser();
	
	public DomainSpecificHeuristicsTest() {
		atomStore = new AtomStoreImpl();
	}
	
	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_AB_SameLevel() {
		Program program = parser.parse(
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic a : not b. [2@1]" + LS +
				"#heuristic b : not a. [1@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}
	
	@Test
	@Ignore("Empty heuristic conditions are not yet supported")
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_AB_SameLevel_EmptyCondition() {
		Program program = parser.parse(
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic a. [2@1]" + LS +
				"#heuristic b. [1@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}
	
	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_TwoDirectivesForSameHead() {
		Program program = parser.parse(
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic a : not b. [3@1]" + LS +
				"#heuristic b : not a. [1@1]" + LS +
				"#heuristic b : not a, not b. [2@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}
	
	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_AB_SameLevel() {
		Program program = parser.parse(
				"n(1)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic a(N) : n(N), not b(N). [2@1]" + LS +
				"#heuristic b(N) : n(N), not a(N). [1@1]");
		solveAndAssertAnswerSets(program, "{ a(1), n(1) }", "{ b(1), n(1) }");
	}
	
	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_AB_OnlyFactsInCondition() {
		Program program = parser.parse(
				"n(1)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic a(N) : n(N). [2@1]" + LS +
				"#heuristic b(N) : n(N). [1@1]");
		solveAndAssertAnswerSets(program, "{ a(1), n(1) }", "{ b(1), n(1) }");
	}
	
	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_AB_DominatingLevel() {
		Program program = parser.parse(
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic a : not b. [1@2]" + LS +
				"#heuristic b : not a. [2@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}
	
	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_AB_DominatingLevel() {
		Program program = parser.parse(
				"n(1)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic a(N) : n(N), not b(N). [1@2]" + LS +
				"#heuristic b(N) : n(N), not a(N). [2@1]");
		solveAndAssertAnswerSets(program, "{ a(1), n(1) }", "{ b(1), n(1) }");
	}

	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_BA_SameLevel() {
		Program program = parser.parse(
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic a : not b. [1@1]" + LS +
				"#heuristic b : not a. [2@1]");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}

	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_BA_SameLevel() {
		Program program = parser.parse(
				"n(1)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic a(N) : n(N), not b(N). [1@1]" + LS +
				"#heuristic b(N) : n(N), not a(N). [2@1]");
		solveAndAssertAnswerSets(program, "{ b(1), n(1) }", "{ a(1), n(1) }");
	}

	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_BA_DominatingLevel() {
		Program program = parser.parse(
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic a : not b. [2@1]" + LS +
				"#heuristic b : not a. [1@2]");
		solveAndAssertAnswerSets(program, "{ b }", "{ a }");
	}

	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_BA_DominatingLevel() {
		Program program = parser.parse(
				"n(1)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic a(N) : n(N), not b(N). [2@1]" + LS +
				"#heuristic b(N) : n(N), not a(N). [1@2]");
		solveAndAssertAnswerSets(program, "{ b(1), n(1) }", "{ a(1), n(1) }");
	}

	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_SimpleChoiceGenerator_NegativeLiteral() {
		Program program = parser.parse(
				"c :- not nc." + LS +
				"nc :- not c." + LS +
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic c : not nc. [10@1]" + LS +
				"#heuristic a : not b, c. [2@1]" + LS +
				"#heuristic b : not a, not c. [2@1]");
		solveAndAssertAnswerSets(program, "{ a, c }", "{ b, c }", "{ b, nc }", "{ a, nc }");
	}

	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_SimpleChoiceGenerator_NegativeLiteral() {
		Program program = parser.parse(
				"n(1)." + LS +
				"c(N) :- n(N), not nc(N)." + LS +
				"nc(N) :- n(N), not c(N)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic c(N) : n(N), not nc(N). [10@1]" + LS +
				"#heuristic a(N) : n(N), not b(N), c(N). [2@1]" + LS +
				"#heuristic b(N) : n(N), not a(N), not c(N). [2@1]");
		solveAndAssertAnswerSets(program, "{ a(1), c(1), n(1) }", "{ b(1), c(1), n(1) }", "{ b(1), n(1), nc(1) }", "{ a(1), n(1), nc(1) }");
	}

	@Test
	public void testSimpleGroundHeuristicProgram_HeuristicDirective_SimpleChoiceGenerator_PositiveLiteral() {
		Program program = parser.parse(
				"c :- not nc." + LS +
				"nc :- not c." + LS +
				"a :- not b." + LS +
				"b :- not a." + LS +
				"#heuristic c : not nc. [10@1]" + LS +
				"#heuristic a : not b, c. [2@1]" + LS +
				"#heuristic b : not a, not c. [2@1]");
		solveAndAssertAnswerSets(program, "{ a, c }", "{ b, c }", "{ b, nc }", "{ a, nc }");
	}

	@Test
	public void testSimpleNonGroundHeuristicProgram_HeuristicDirective_SimpleChoiceGenerator_PositiveLiteral() {
		Program program = parser.parse(
				"n(1)." + LS +
				"c(N) :- n(N), not nc(N)." + LS +
				"nc(N) :- n(N), not c(N)." + LS +
				"a(N) :- n(N), not b(N)." + LS +
				"b(N) :- n(N), not a(N)." + LS +
				"#heuristic c(N) : n(N), not nc(N). [10@1]" + LS +
				"#heuristic a(N) : n(N), not b(N), c(N). [2@1]" + LS +
				"#heuristic b(N) : n(N), not a(N), not c(N). [2@1]");
		solveAndAssertAnswerSets(program, "{ a(1), c(1), n(1) }", "{ b(1), c(1), n(1) }", "{ b(1), n(1), nc(1) }", "{ a(1), n(1), nc(1) }");
	}
	
	@Test
	public void testSimpleHeuristicProgram_HeuristicDirective_SimpleNonGroundArithmetics() {
		Program program = parser.parse(
				"n(2)." + LS +
				"a :- n(N), not b." + LS +
				"b :- not a." + LS +
				"#heuristic a : n(N), not b, Np1=N+1. [Np1@1]" + LS +
				"#heuristic b : not a. [1]");
		solveAndAssertAnswerSets(program, "{ a, n(2) }", "{ b, n(2) }");
	}

	private void solveAndAssertAnswerSets(Program program, String... expectedAnswerSets) {
		HeuristicsConfiguration heuristicsConfiguration = HeuristicsConfiguration.builder().setHeuristic(Heuristic.NAIVE).build();
		Solver solver = SolverFactory.getInstance("default", "alpharoaming", atomStore, GrounderFactory.getInstance("naive", program, atomStore), new Random(), heuristicsConfiguration, true, false);
		assertEquals(Arrays.asList(expectedAnswerSets), solver.stream().map(AnswerSet::toString).collect(Collectors.toList()));
	}
}
