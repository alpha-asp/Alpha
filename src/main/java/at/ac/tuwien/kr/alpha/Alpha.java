/**
 * Copyright (c) 2017-2019, the Alpha Team.
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
package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.config.InputConfig;
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfigurationBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class Alpha {

	private SystemConfig config = new SystemConfig(); // config is initialized with default values

	public Alpha(SystemConfig cfg) {
		this.config = cfg;
	}

	public Alpha() {
	}

	public Program readProgram(InputConfig cfg) throws IOException {
		Program retVal = new Program();
		if (!cfg.getFiles().isEmpty()) {
			retVal.accumulate(this.readProgramFiles(cfg.isLiterate(), cfg.getPredicateMethods(), cfg.getFiles()));
		}
		if (!cfg.getAspStrings().isEmpty()) {
			retVal.accumulate(this.readProgramString(StringUtils.join(cfg.getAspStrings(), System.lineSeparator()), cfg.getPredicateMethods()));
		}
		return retVal;
	}

	public Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException {
		ProgramParser parser = new ProgramParser(externals);
		Program retVal = new Program();
		for (String fileName : paths) {
			CharStream stream;
			if (!literate) {
				stream = CharStreams.fromFileName(fileName);
			} else {
				stream = CharStreams.fromChannel(Util.streamToChannel(Util.literate(Files.lines(Paths.get(fileName)))), 4096, CodingErrorAction.REPLACE,
						fileName);
			}
			retVal.accumulate(parser.parse(stream));
		}
		return retVal;
	}

	public Program readProgramString(String aspString, Map<String, PredicateInterpretation> externals) {
		ProgramParser parser = new ProgramParser(externals);
		return parser.parse(aspString);
	}

	/**
	 * Prepares a solver (and accompanying grounder) instance pre-loaded with the given program. Use this if the solver is needed after reading answer sets
	 * (e.g. for obtaining statistics)
	 * 
	 * @param program the program to solve
	 * @return a solver (and accompanying grounder) instance pre-loaded with the given program
	 */
	public Solver prepareSolverFor(Program program) {
		String grounderName = this.config.getGrounderName();
		String solverName = this.config.getSolverName();
		String nogoodStoreName = this.config.getNogoodStoreName();
		long seed = this.config.getSeed();
		boolean doDebugChecks = this.config.isDebugInternalChecks();
		boolean disableJustificationSearch = this.config.isDisableJustificationSearch();
		
		HeuristicsConfigurationBuilder heuristicsConfigurationBuilder = HeuristicsConfiguration.builder();
		heuristicsConfigurationBuilder.setHeuristic(this.config.getBranchingHeuristic());
		heuristicsConfigurationBuilder.setMomsStrategy(this.config.getMomsStrategy());
		heuristicsConfigurationBuilder.setReplayChoices(this.config.getReplayChoices());

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance(grounderName, program, atomStore, doDebugChecks);

		Solver solver = SolverFactory.getInstance(solverName, nogoodStoreName, atomStore, grounder, new Random(seed),
				heuristicsConfigurationBuilder.build(), doDebugChecks, disableJustificationSearch);
		return solver;
	}

	public Stream<AnswerSet> solve(Program program) {
		Stream<AnswerSet> retVal = this.prepareSolverFor(program).stream();
		return this.config.isSortAnswerSets() ? retVal.sorted() : retVal;
	}

	public SystemConfig getConfig() {
		return this.config;
	}

	public void setConfig(SystemConfig config) {
		this.config = config;
	}

}
