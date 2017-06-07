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
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

@RunWith(Parameterized.class)
public abstract class AbstractSolverTests {
	@Parameters(name = "{0}")
	public static Collection<Object[]> factories() {
		boolean enableAdditionalInternalChecks = false;
		Collection<Object[]> factories = new ArrayList<>();
		factories.add(new Object[] {"NS", (Function<Grounder, Solver>) (Grounder g) -> {
			ArrayAssignment assignment = new ArrayAssignment();
			NoGoodStore store = new NoGoodStoreAlphaRoaming(assignment);
			return new NaiveSolver(g, store, assignment);
		}});
		factories.add(new Object[] {"NS/naive", (Function<Grounder, Solver>) (Grounder g) -> {
			ArrayAssignment assignment = new ArrayAssignment();
			NoGoodStore store = new NaiveNoGoodStore(assignment);
			return new NaiveSolver(g, store, assignment);
		}});
		for (Heuristic heuristic : Heuristic.values()) {
			String name = "DS/R/" + heuristic;
			Function<Grounder, Solver> instantiator = g -> {
				ArrayAssignment assignment = new ArrayAssignment();
				NoGoodStore store = new NoGoodStoreAlphaRoaming(assignment);
				return new DefaultSolver(g, store, assignment, new Random(), heuristic, enableAdditionalInternalChecks);
			};
			factories.add(new Object[] {name, instantiator});
			name = "DS/R/" + heuristic + "/naive";
			instantiator = g -> {
				ArrayAssignment assignment = new ArrayAssignment();
				NoGoodStore store = new NaiveNoGoodStore(assignment);
				return new DefaultSolver(g, store, assignment, new Random(), heuristic, enableAdditionalInternalChecks);
			};
			factories.add(new Object[] {name, instantiator});
			name = "DS/D/" + heuristic;
			instantiator = g -> {
				ArrayAssignment assignment = new ArrayAssignment();
				NoGoodStore store = new NoGoodStoreAlphaRoaming(assignment);
				return new DefaultSolver(g, store, assignment, new Random(0), heuristic, enableAdditionalInternalChecks);
			};
			factories.add(new Object[] {name, instantiator});
			name = "DS/D/" + heuristic + "/naive";
			instantiator = g -> {
				ArrayAssignment assignment = new ArrayAssignment();
				NoGoodStore store = new NaiveNoGoodStore(assignment);
				return new DefaultSolver(g, store, assignment, new Random(0), heuristic, enableAdditionalInternalChecks);
			};
			factories.add(new Object[] {name, instantiator});
		}
		return factories;
	}

	@Parameter(value = 0)
	public String solverName;

	@Parameter(value = 1)
	public Function<Grounder, Solver> factory;

	protected Solver getInstance(Grounder g) {
		return factory.apply(g);
	}
}
