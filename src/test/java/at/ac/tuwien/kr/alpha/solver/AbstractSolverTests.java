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

import at.ac.tuwien.kr.alpha.AnswerSetsParser;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public abstract class AbstractSolverTests {
	private final ProgramParser parser = new ProgramParser();

	/**
	 * Sets the logging level to TRACE. Useful for debugging; call at beginning of test case.
	 */
	private static void enableTracing() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(ch.qos.logback.classic.Level.TRACE);
	}

	private static void enableDebugLog() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
	}

	private static String[] getProperty(String subKey, String def) {
		return System.getProperty("test." + subKey, def).split(",");
	}

	@Parameters(name = "{0}/{1}/{2}/{3}")
	public static Collection<Object[]> parameters() {
		// Check whether we are running in a CI environment.
		boolean ci = Boolean.valueOf(System.getenv("CI"));

		String[] solvers = getProperty("solvers", ci ? "default" : "default,naive");
		String[] grounders = getProperty("grounders", "naive");
		String[] stores = getProperty("stores", ci ? "alpharoaming" : "alpharoaming,naive");
		String[] heuristics = getProperty("heuristics", ci ? "ALL" : "NAIVE");

		// "ALL" is a magic value that will be expanded to contain all heuristics.
		if ("ALL".equals(heuristics[0])) {
			BranchingHeuristicFactory.Heuristic[] values = BranchingHeuristicFactory.Heuristic.values();
			heuristics = new String[values.length];
			int i = 0;
			for (BranchingHeuristicFactory.Heuristic heuristic : values) {
				heuristics[i++] = heuristic.toString();
			}
		}

		// NOTE:
		// It is handy to set the seed for reproducing bugs. However, the reverse is also sometimes needed:
		// A test case fails, now what was the seed that "caused" it? To allow this, we need full control over
		// the seed, so we generate one in any case.
		// If your test case fails you can inspect the property called "seed" of AbstractSolverTests and extract
		// its value.
		String seedProperty = System.getProperty("seed", ci ? "0" : "");
		long seed = seedProperty.isEmpty() ? (new Random().nextLong()) : Long.valueOf(seedProperty);

		boolean checks = true;

		Collection<Object[]> factories = new ArrayList<>();

		for (String solver : solvers) {
			for (String grounder : grounders) {
				for (String store : stores) {
					for (String heuristic : heuristics) {
						factories.add(new Object[]{
							solver, grounder, store, BranchingHeuristicFactory.Heuristic.valueOf(heuristic), seed, checks
						});
					}
				}
			}
		}

		return factories;
	}

	@Parameter(0)
	public String solverName;

	@Parameter(1)
	public String grounderName;

	@Parameter(2)
	public String storeName;

	@Parameter(3)
	public BranchingHeuristicFactory.Heuristic heuristic;

	@Parameter(4)
	public long seed;

	@Parameter(5)
	public boolean checks;

	protected Solver getInstance(Grounder grounder) {
		return SolverFactory.getInstance(solverName, storeName, grounder, new Random(seed), heuristic, checks);
	}

	protected Solver getInstance(Program program) {
		return getInstance(GrounderFactory.getInstance(grounderName, program));
	}

	protected Solver getInstance(String program) {
		return getInstance(CharStreams.fromString(program));
	}

	protected Solver getInstance(CharStream program) {
		try {
			return getInstance(parser.parse(program));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Set<AnswerSet> collectSet(String program) {
		return getInstance(program).collectSet();
	}

	protected Set<AnswerSet> collectSet(Program program) {
		return getInstance(program).collectSet();
	}

	protected Set<AnswerSet> collectSet(CharStream program) {
		return getInstance(program).collectSet();
	}

	protected void assertAnswerSets(String program, String... answerSets) throws IOException {
		if (answerSets.length == 0) {
			assertAnswerSets(program, emptySet());
			return;
		}

		StringJoiner joiner = new StringJoiner("} {", "{", "}");
		Arrays.stream(answerSets).forEach(joiner::add);
		assertAnswerSets(program, AnswerSetsParser.parse(joiner.toString()));
	}

	protected void assertAnswerSet(String program, String answerSet) throws IOException {
		assertAnswerSets(program, AnswerSetsParser.parse("{ " + answerSet + " }"));
	}

	protected void assertAnswerSetsWithBase(String program, String base, String... answerSets) throws IOException {
		base = base.trim();
		if (!base.endsWith(",")) {
			base += ", ";
		}

		for (int i = 0; i < answerSets.length; i++) {
			answerSets[i] = base + answerSets[i];
			// Remove trailing ",".
			answerSets[i] = answerSets[i].trim();
			if (answerSets[i].endsWith(",")) {
				answerSets[i] = answerSets[i].substring(0, answerSets[i].length() - 1);
			}
		}

		assertAnswerSets(program, answerSets);
	}

	protected void assertAnswerSets(String program, Set<AnswerSet> answerSets) throws IOException {
		assertEquals(answerSets, collectSet(program));
	}
}
