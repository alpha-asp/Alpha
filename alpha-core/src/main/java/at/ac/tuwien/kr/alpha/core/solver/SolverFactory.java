/**
 * Copyright (c) 2016-2021, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;

public final class SolverFactory {

	private final String solverName;
	private final String nogoodStoreName;
	private final SolverConfig solverConfig;

	public SolverFactory(String solverName, String noGoodStoreName, SolverConfig solverConfig) {
		this.solverName = solverName;
		this.nogoodStoreName = noGoodStoreName;
		this.solverConfig = solverConfig;
	}

	public Solver createSolver(Grounder grounder, AtomStore atomStore) {
		return SolverFactory.createSolver(solverName, nogoodStoreName, solverConfig, atomStore, grounder);
	}

	private static Solver createSolver(String solverName, String nogoodStoreName, SolverConfig solverConfig, AtomStore atomStore, Grounder grounder) {
		final WritableAssignment assignment = new TrailAssignment(atomStore, solverConfig.isEnableDebugChecks());

		NoGoodStore store;

		switch (nogoodStoreName.toLowerCase()) {
			case "naive":
				store = new NaiveNoGoodStore(assignment);
				break;
			case "alpharoaming":
				store = new NoGoodStoreAlphaRoaming(assignment, solverConfig.isEnableDebugChecks());
				break;
			default:
				throw new IllegalArgumentException("Unknown store requested.");
		}

		switch (solverName.toLowerCase()) {
			case "naive":
				return new NaiveSolver(atomStore, grounder);
			case "default":
				return new DefaultSolver(solverConfig, atomStore, grounder, store, assignment);
		}
		throw new IllegalArgumentException("Unknown solver requested.");
	}

}
