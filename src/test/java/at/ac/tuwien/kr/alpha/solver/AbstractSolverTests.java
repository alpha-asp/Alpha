/**
 * Copyright (c) 2017, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
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

import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.function.Function;

@RunWith(Parameterized.class)
public abstract class AbstractSolverTests {
	@Parameters(name = "{0}")
	public static Collection<Object[]> factories() {
		boolean enableAdditionalInternalChecks = false;
		return Arrays.asList(new Object[][]{
			{
				"NaiveSolver",
				(Function<Grounder, Solver>) NaiveSolver::new
			},
			{
				"DefaultSolver (random AlphaHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.ALPHA, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic AlphaHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.ALPHA, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (random AlphaRandomSignHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.ALPHA_RANDOM_SIGN, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic AlphaRandomSignHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.ALPHA_RANDOM_SIGN, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (random AlphaActiveRuleHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.ALPHA_ACTIVE_RULE, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic AlphaActiveRuleHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.ALPHA_ACTIVE_RULE, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (random AlphaHeadMustBeTrueHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.ALPHA_HEAD_MBT, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic AlphaHeadMustBeTrueHeuristic)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.ALPHA_HEAD_MBT, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (random BerkMin)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.BERKMIN, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic BerkMin)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.BERKMIN, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (random BerkMinLiteral)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.BERKMINLITERAL, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic BerkMinLiteral)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.BERKMINLITERAL, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (random Naive)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(), BranchingHeuristicFactory.NAIVE, enableAdditionalInternalChecks);
				}
			},
			{
				"DefaultSolver (deterministic Naive)",
				(Function<Grounder, Solver>) g -> {
					return new DefaultSolver(g, new Random(0), BranchingHeuristicFactory.NAIVE, enableAdditionalInternalChecks);
				}
			},
		});
	}

	@Parameter(value = 0)
	public String solverName;

	@Parameter(value = 1)
	public Function<Grounder, Solver> factory;

	protected Solver getInstance(Grounder g) {
		return factory.apply(g);
	}
}
