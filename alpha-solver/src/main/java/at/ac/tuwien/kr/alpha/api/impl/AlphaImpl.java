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
package at.ac.tuwien.kr.alpha.api.impl;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.grounder.heuristics.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.common.CoreAnswerSet;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.core.solver.Solver;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.core.util.Util;

public class AlphaImpl implements Alpha {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlphaImpl.class);

	private SystemConfig config = new SystemConfig(); // The config is initialized with default values.

	public AlphaImpl(SystemConfig cfg) {
		this.config = cfg;
	}

	public AlphaImpl() {
	}

	@Override
	public InputProgramImpl readProgram(InputConfig cfg) throws IOException {
		InputProgramImpl.Builder prgBuilder = InputProgramImpl.builder();
		InputProgramImpl tmpProg;
		if (!cfg.getFiles().isEmpty()) {
			tmpProg = readProgramFiles(cfg.isLiterate(), cfg.getPredicateMethods(), cfg.getFiles());
			prgBuilder.accumulate(tmpProg);
		}
		if (!cfg.getAspStrings().isEmpty()) {
			tmpProg = readProgramString(StringUtils.join(cfg.getAspStrings(), System.lineSeparator()), cfg.getPredicateMethods());
			prgBuilder.accumulate(tmpProg);
		}
		return prgBuilder.build();
	}

	@Override
	public InputProgramImpl readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException {
		return readProgramFiles(literate, externals, paths.stream().map(Paths::get).collect(Collectors.toList()).toArray(new Path[] {}));
	}

	@Override
	public InputProgramImpl readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException {
		ProgramParserImpl parser = new ProgramParserImpl(externals);
		InputProgramImpl.Builder prgBuilder = InputProgramImpl.builder();
		InputProgramImpl tmpProg;
		for (Path path : paths) {
			CharStream stream;
			if (!literate) {
				stream = CharStreams.fromPath(path);
			} else {
				stream = CharStreams.fromChannel(Util.streamToChannel(Util.literate(Files.lines(path))), 4096, CodingErrorAction.REPLACE, path.toString());
			}
			tmpProg = parser.parse(stream);
			prgBuilder.accumulate(tmpProg);
		}
		return prgBuilder.build();
	}

	@Override
	public InputProgramImpl readProgramString(String aspString, Map<String, PredicateInterpretation> externals) {
		ProgramParserImpl parser = new ProgramParserImpl(externals);
		return parser.parse(aspString);
	}

	@Override
	public InputProgramImpl readProgramString(String aspString) {
		return readProgramString(aspString, null);
	}

	// TODO make sure to adapt this without exposing internal imnplementation types
	public NormalProgram normalizeProgram(InputProgramImpl program) {
		return new NormalizeProgramTransformation(config.isUseNormalizationGrid()).apply(program);
	}

	// TODO make sure to adapt this without exposing internal imnplementation types
	public InternalProgram performProgramPreprocessing(InternalProgram program) {
		LOGGER.debug("Preprocessing InternalProgram!");
		InternalProgram retVal = program;
		if (config.isEvaluateStratifiedPart()) {
			AnalyzedProgram analyzed = new AnalyzedProgram(program.getRules(), program.getFacts());
			retVal = new StratifiedEvaluation().apply(analyzed);
		}
		return retVal;
	}

	// TODO make sure to adapt this without exposing internal imnplementation types
	public InternalProgram performProgramPreprocessing(AnalyzedProgram program) {
		LOGGER.debug("Preprocessing AnalyzedProgram!");
		InternalProgram retVal = program;
		if (config.isEvaluateStratifiedPart()) {
			retVal = new StratifiedEvaluation().apply(program);
		}
		return retVal;
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}) for cases where details of the
	 * program analysis and normalization aren't of interest.
	 */
	public Stream<AnswerSet> solve(InputProgramImpl program) {
		return solve(program, InputConfig.DEFAULT_FILTER);
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}, {@link Predicate}) for cases where
	 * details of the program analysis and normalization aren't of interest.
	 */
	public Stream<AnswerSet> solve(InputProgramImpl program, java.util.function.Predicate<Predicate> filter) {
		NormalProgram normalized = normalizeProgram(program);
		return solve(normalized, filter);
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}) for cases where details of the
	 * program analysis aren't of interest.
	 */
	public Stream<AnswerSet> solve(NormalProgram program, java.util.function.Predicate<Predicate> filter) {
		InternalProgram preprocessed = performProgramPreprocessing(InternalProgram.fromNormalProgram(program));
		return solve(preprocessed, filter);
	}

	/**
	 * Overloaded version of solve({@link InternalProgram}, {@link Predicate}) that uses a default filter (accept
	 * everything).
	 * 
	 * @param program the program to solve
	 * @return a stream of answer sets
	 */
	public Stream<AnswerSet> solve(InternalProgram program) {
		return solve(program, InputConfig.DEFAULT_FILTER);
	}

	/**
	 * Solves the given program and filters answer sets based on the passed predicate.
	 * 
	 * @param program an {@link InternalProgram} to solve
	 * @param filter  {@link Predicate} filtering {@at.ac.tuwien.kr.alpha.common.Predicate}s in the returned answer sets
	 * @return a Stream of answer sets representing stable models of the given program
	 */
	public Stream<AnswerSet> solve(InternalProgram program, java.util.function.Predicate<Predicate> filter) {
		Stream<AnswerSet> retVal = prepareSolverFor(program, filter).stream();
		return config.isSortAnswerSets() ? retVal.sorted() : retVal;
	}

	/**
	 * Prepares a solver (and accompanying grounder) instance pre-loaded with the given program. Use this if the
	 * solver is needed after reading answer sets (e.g. for obtaining statistics).
	 * 
	 * @param program the program to solve.
	 * @param filter  a (java util) predicate that filters (asp-)predicates which should be contained in the answer
	 *                set stream from the solver.
	 * @return a solver (and accompanying grounder) instance pre-loaded with the given program.
	 */
	public Solver prepareSolverFor(InternalProgram program, java.util.function.Predicate<Predicate> filter) {
		String grounderName = config.getGrounderName();
		boolean doDebugChecks = config.isDebugInternalChecks();

		GrounderHeuristicsConfiguration grounderHeuristicConfiguration = GrounderHeuristicsConfiguration
				.getInstance(config.getGrounderToleranceConstraints(), config.getGrounderToleranceRules());
		grounderHeuristicConfiguration.setAccumulatorEnabled(config.isGrounderAccumulatorEnabled());

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance(grounderName, program, atomStore, filter, grounderHeuristicConfiguration, doDebugChecks);

		return SolverFactory.getInstance(config, atomStore, grounder);
	}

	public SystemConfig getConfig() {
		return config;
	}

	public void setConfig(SystemConfig config) {
		this.config = config;
	}

}
