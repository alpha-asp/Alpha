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
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.DefaultSolver;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DomainSpecific} heuristics.
 */
public class DomainSpecificHeuristicsTest {
	
	private static final String LS = System.lineSeparator();
	private final AtomStore atomStore;
	private final ProgramParser parser = new ProgramParser();
	private final HeuristicsConfiguration heuristicsConfiguration;
	private final SystemConfig systemConfig;

	public DomainSpecificHeuristicsTest() {
		atomStore = new AtomStoreImpl();
		heuristicsConfiguration = HeuristicsConfiguration.builder().setHeuristic(Heuristic.NAIVE).build();
		systemConfig = buildSystemConfig();
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

	@Test
	public void testTwoHeuristicsWithSameHeadAndPriority() {
		Program program = parser.parse(
				"a :- not b." + LS +
						"b :- not a." + LS +
						"#heuristic a : not b. [2@1]" + LS +
						"#heuristic a : not a. [2@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}

	@Test
	public void testTwoHeuristicsOneWithoutRule() {
		Program program = parser.parse(
				"a :- not b." + LS +
						"b :- not a." + LS +
						"#heuristic a : not b. [2@1]" + LS +
						"#heuristic c : not a. [3@1]");
		solveAndAssertAnswerSets(program, "{ a }", "{ b }");
	}

	/**
	 * Uses a modified (smaller) PUP encoding and instance to test for an issue with domain-specific heuristics
	 * that led to heuristics not being applicable after backjumping.
	 */
	@Test
	public void testHeuristicApplicableAfterBackjump() {
		Program program = parser.parse(
			"comUnit(1)." + LS +
				"comUnit(2)." + LS +
				"maxUnit(2)." + LS +
				"assign(1, z, 1)." + LS +
				"order(4, s, 4)." + LS +
				"order(3, s, 4)." + LS +
				"order(2, z, 3)." + LS +
				"order(2, s, 2)." + LS +
				"order(1, s, 2)." + LS +
				"order(1, z, 1)." + LS +
				"maxElem(6)." + LS +
				"maxOrder(4)." + LS +
				"elem(z,Z) :- order(Z,z,_)." + LS +
				"elem(s,S) :- order(S,s,_)." + LS +
				"  assign(U,T,X) :- elem(T,X), comUnit(U), not n_assign(U,T,X)." + LS +
				"n_assign(U,T,X) :- elem(T,X), comUnit(U), not   assign(U,T,X)." + LS +
				"h_assign_1(U1,T,X,W) :- order(X,T,O), assign(U,T1,Y), order(Y,T1,Om1), Om1=O-1, maxOrder(M), maxUnit(MU), comUnit(U1), U1<=U, maxElem(ME), W=10*MU*(M-O)+2*(ME-X)+U1." + LS +
				"#heuristic assign(U,T,X) : h_assign_1(U,T,X,W). [W]" + LS +
				"h_assign_2(Up1,T,X,W) :- order(X,T,O), assign(U,T1,Y), order(Y,T1,Om1), Om1=O-1, maxOrder(M), maxUnit(MU), comUnit(Up1), Up1=U+1, maxElem(ME), W=10*MU*(M-O)+2*(ME-X)." + LS +
				"#heuristic assign(U,T,X) : h_assign_2(U,T,X,W). [W]" + LS +
				":- assign(U,T,X1), assign(U,T,X2), assign(U,T,X3), X1<X2, X2<X3." + LS +
				":- assign(U1,T,X), assign(U2,T,X), U1<U2." + LS +
				"atLeastOneUnit(T,X):- assign(_,T,X)." + LS +
				":- elem(T,X), not atLeastOneUnit(T,X)." + LS +
				"partnerunits(U,P) :- assign(U,z,Z), assign(P,s,D), zone2sensor(Z,D), U!=P." + LS +
				"partnerunits(U,P) :- partnerunits(P,U)." + LS +
				":- partnerunits(U,P1), partnerunits(U,P2), partnerunits(U,P3), P1<P2, P2<P3."
		);

		Solver solver = SolverFactory.getInstance(systemConfig, atomStore, GrounderFactory.getInstance("naive", program, atomStore, heuristicsConfiguration, true), heuristicsConfiguration);
		solver.stream().limit(1).collect(Collectors.toList()).get(0);
		DefaultSolver defaultSolver = (DefaultSolver) solver;
		assertTrue("No backjumps done", defaultSolver.getNumberOfBackjumps() > 0);
		Map<String, Integer> numberOfChoicesPerBranchingHeuristic = defaultSolver.getNumberOfChoicesPerBranchingHeuristic();
		assertTrue("No numbers of choices per branching heuristic", !numberOfChoicesPerBranchingHeuristic.isEmpty());
		for (Map.Entry<String, Integer> entry : numberOfChoicesPerBranchingHeuristic.entrySet()) {
			if (entry.getKey().equals("DomainSpecific")) {
				assertTrue("No choices done by DomainSpecific", entry.getValue() > 0);
			} else if (entry.getKey().equals("NaiveHeuristic")) {
				// default heuristic has to decide two choice points that are not covered by the domain-specific one
				assertEquals("Unexpected number of choices done by Naive", Integer.valueOf(2), entry.getValue());
			} else {
				assertEquals("Choices done by " + entry.getKey(), Integer.valueOf(0), entry.getValue());
			}
		}

	}

	private void solveAndAssertAnswerSets(Program program, String... expectedAnswerSets) {
		HeuristicsConfiguration heuristicsConfiguration = HeuristicsConfiguration.builder().setHeuristic(Heuristic.NAIVE).build();
		Solver solver = SolverFactory.getInstance(systemConfig, atomStore, GrounderFactory.getInstance("naive", program, atomStore, heuristicsConfiguration, true), heuristicsConfiguration);
		assertEquals(Arrays.asList(expectedAnswerSets), solver.stream().map(AnswerSet::toString).collect(Collectors.toList()));
	}

	private SystemConfig buildSystemConfig() {
		SystemConfig config = new SystemConfig();
		config.setSolverName("default");
		config.setNogoodStoreName("alpharoaming");
		config.setBranchingHeuristic(heuristicsConfiguration.getHeuristic());
		config.setDebugInternalChecks(true);
		config.setDisableJustificationSearch(false);
		return config;
	}
}
