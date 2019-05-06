/**
 * Copyright (c) 2017-2018, the Alpha Team.
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

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.lang3.StringUtils;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.NormalProgram;
import at.ac.tuwien.kr.alpha.config.InputConfig;
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.impl.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfigurationBuilder;

public class Alpha {

	private SystemConfig config = new SystemConfig(); // config is initialized with default values

	public Alpha(SystemConfig cfg) {
		this.config = cfg;
	}

	public Alpha() {
	}

	public InputProgram readProgram(InputConfig cfg) throws IOException {
		InputProgram.Builder prgBuilder = InputProgram.builder();
		InputProgram tmpProg;
		if (!cfg.getFiles().isEmpty()) {
			tmpProg = this.readProgramFiles(cfg.isLiterate(), cfg.getPredicateMethods(), cfg.getFiles());
			prgBuilder.addFacts(tmpProg.getFacts());
			prgBuilder.addRules(tmpProg.getRules());
			prgBuilder.addInlineDirectives(tmpProg.getInlineDirectives());
		}
		if (!cfg.getAspStrings().isEmpty()) {
			tmpProg = this.readProgramString(StringUtils.join(cfg.getAspStrings(), "\n"), cfg.getPredicateMethods());
			prgBuilder.addFacts(tmpProg.getFacts());
			prgBuilder.addRules(tmpProg.getRules());
			prgBuilder.addInlineDirectives(tmpProg.getInlineDirectives());
		}
		return prgBuilder.build();
	}

	public InternalProgram performProgramPreprocessing(InputProgram program) {
		NormalProgram normalProg = new NormalizeProgramTransformation(this.config.isUseNormalizationGrid()).apply(program);
		return InternalProgram.fromNormalProgram(normalProg);
	}

	public InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException {
		return this.readProgramFiles(literate, externals, paths.stream().map(Paths::get).collect(Collectors.toList()).toArray(new Path[] {}));
	}

	public InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException {
		ProgramParser parser = new ProgramParser(externals);
		InputProgram.Builder prgBuilder = InputProgram.builder();
		InputProgram tmpProg;
		for (Path path : paths) {
			CharStream stream;
			if (!literate) {
				stream = CharStreams.fromPath(path);
			} else {
				stream = CharStreams.fromChannel(Util.streamToChannel(Util.literate(Files.lines(path))), 4096, CodingErrorAction.REPLACE, path.toString());
			}
			tmpProg = parser.parse(stream);
			prgBuilder.addFacts(tmpProg.getFacts());
			prgBuilder.addRules(tmpProg.getRules());
			prgBuilder.addInlineDirectives(tmpProg.getInlineDirectives());
		}
		return prgBuilder.build();
	}

	public InputProgram readProgramString(String aspString, Map<String, PredicateInterpretation> externals) {
		ProgramParser parser = new ProgramParser(externals);
		return parser.parse(aspString);
	}

	public InputProgram readProgramString(String aspString) {
		return this.readProgramString(aspString, null);
	}

	/**
	 * Prepares a solver (and accompanying grounder) instance pre-loaded with the given program. Use this if the solver is needed after reading answer sets
	 * (e.g. for obtaining statistics)
	 * 
	 * @param program the program to solve
	 * @return a solver (and accompanying grounder) instance pre-loaded with the given program
	 */
	public Solver prepareSolverFor(InternalProgram program) {
		String grounderName = this.config.getGrounderName();
		String solverName = this.config.getSolverName();
		String nogoodStoreName = this.config.getNogoodStoreName();
		long seed = this.config.getSeed();
		boolean doDebugChecks = this.config.isDebugInternalChecks();
		boolean disableJustificationSearch = this.config.isDisableJustificationSearch();
		
		HeuristicsConfigurationBuilder heuristicsConfigurationBuilder = HeuristicsConfiguration.builder();
		heuristicsConfigurationBuilder.setHeuristic(this.config.getBranchingHeuristic());
		heuristicsConfigurationBuilder.setMomsStrategy(this.config.getMomsStrategy());

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance(grounderName, program, atomStore, doDebugChecks);

		Solver solver = SolverFactory.getInstance(solverName, nogoodStoreName, atomStore, grounder, new Random(seed),
				heuristicsConfigurationBuilder.build(), doDebugChecks, disableJustificationSearch);
		return solver;
	}

	/**
	 * Convenience method - overloaded version of prepareSolverFor(AnalyzedNormalProgram) for cases where details of the program analysis are not of interest
	 * 
	 * @param program a NormalProgram to solve
	 * @return a solver (and accompanying grounder) instance pre-loaded with the given program
	 */
	public Solver prepareSolverFor(NormalProgram program) {
		return this.prepareSolverFor(InternalProgram.fromNormalProgram(program));
	}

	/**
	 * Convenience method - overloaded version of prepareSolverFor(AnalyzedNormalProgram) for cases where details of the program analysis and program
	 * normalization are not of interest
	 * 
	 * @param program a NormalProgram to solve
	 * @return a solver (and accompanying grounder) instance pre-loaded with the given program
	 */
	public Solver prepareSolverFor(InputProgram program) {
		return this.prepareSolverFor(this.performProgramPreprocessing(program));
	}

	/**
	 * Solves the given program
	 * 
	 * @param program an AnalyzedNormalProgram to solve
	 * @return a Stream of answer sets representing stable models of the given program
	 */
	public Stream<AnswerSet> solve(InternalProgram program) {
		Stream<AnswerSet> retVal = this.prepareSolverFor(program).stream();
		return this.config.isSortAnswerSets() ? retVal.sorted() : retVal;
	}

	/**
	 * Convenience method - overloaded version of solve(AnalyzedNormalProgram) for cases where details of the program analysis aren't of interest
	 */
	public Stream<AnswerSet> solve(NormalProgram program) {
		return this.solve(InternalProgram.fromNormalProgram(program));
	}

	/**
	 * Convenience method - overloaded version of solve(AnalyzedNormalProgram) for cases where details of the program analysis and normalization aren't of
	 * interest
	 */
	public Stream<AnswerSet> solve(InputProgram program) {
		return this.solve(this.performProgramPreprocessing(program));
	}

	public SystemConfig getConfig() {
		return this.config;
	}

	public void setConfig(SystemConfig config) {
		this.config = config;
	}

}
