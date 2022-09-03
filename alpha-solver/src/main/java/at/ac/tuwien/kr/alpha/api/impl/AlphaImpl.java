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
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.DebugSolvingContext;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;
import at.ac.tuwien.kr.alpha.commons.util.Util;
import at.ac.tuwien.kr.alpha.core.actions.ActionExecutionService;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ProgramTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;

public class AlphaImpl implements Alpha {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlphaImpl.class);

	private final Supplier<ProgramParser> parserFactory;
	private final Supplier<ProgramTransformer<InputProgram, NormalProgram>> programNormalizationFactory;

	private final GrounderFactory grounderFactory;
	private final SolverFactory solverFactory;


	private final boolean sortAnswerSets;

	AlphaImpl(Supplier<ProgramParser> parserFactory, Supplier<ProgramTransformer<InputProgram, NormalProgram>> programNormalizationFactory,
			GrounderFactory grounderFactory,
			SolverFactory solverFactory,
			boolean enableStratifiedEvaluation, boolean sortAnswerSets) {
		this.parserFactory = parserFactory;
		this.programNormalizationFactory = programNormalizationFactory;
		this.grounderFactory = grounderFactory;
		this.solverFactory = solverFactory;
		this.sortAnswerSets = sortAnswerSets;
	}

	@Override
	public InputProgram readProgram(InputConfig cfg) throws IOException {
		InputProgramImpl.Builder prgBuilder = InputProgramImpl.builder();
		InputProgram tmpProg;
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
	public InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException {
		return readProgramFiles(literate, externals, paths.stream().map(Paths::get).collect(Collectors.toList()).toArray(new Path[] {}));
	}

	@Override
	@SuppressWarnings("resource")
	public InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException {
		ProgramParser parser = parserFactory.get();
		InputProgramImpl.Builder prgBuilder = InputProgramImpl.builder();
		InputProgram tmpProg;
		for (Path path : paths) {
			InputStream stream;
			if (!literate) {
				stream = Files.newInputStream(path);
			} else {
				stream = Channels.newInputStream(Util.streamToChannel(Util.literate(Files.lines(path))));
			}
			tmpProg = parser.parse(stream, externals);
			prgBuilder.accumulate(tmpProg);
		}
		return prgBuilder.build();
	}

	@Override
	public InputProgram readProgramString(String aspString, Map<String, PredicateInterpretation> externals) {
		return parserFactory.get().parse(aspString, externals);
	}

	@Override
	public InputProgram readProgramString(String aspString) {
		return readProgramString(aspString, Collections.emptyMap());
	}

	@Override
	public NormalProgram normalizeProgram(InputProgram program) {
		return programNormalizationFactory.get().transform(program);
	}

	@VisibleForTesting
	InternalProgram performProgramPreprocessing(NormalProgram program) {
		LOGGER.debug("Preprocessing InternalProgram!");
		InternalProgram retVal = InternalProgram.fromNormalProgram(program);
		// TODO get the StratifiedEvaluation from factory
		AnalyzedProgram analyzed = new AnalyzedProgram(retVal.getRules(), retVal.getFacts());
		retVal = new StratifiedEvaluation(actionContext, true).transform(analyzed);
		return retVal;
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}) for cases where details of the
	 * program analysis and normalization aren't of interest.
	 */
	@Override
	public Stream<AnswerSet> solve(InputProgram program) {
		return solve(program, InputConfig.DEFAULT_FILTER);
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}, {@link Predicate}) for cases where
	 * details of the program analysis and normalization aren't of interest.
	 */
	@Override
	public Stream<AnswerSet> solve(InputProgram program, java.util.function.Predicate<Predicate> filter) {
		NormalProgram normalized = normalizeProgram(program);
		return solve(normalized, filter);
	}

	@Override
	public Stream<AnswerSet> solve(NormalProgram program) {
		return solve(program, InputConfig.DEFAULT_FILTER);
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}) for cases where details of the
	 * program analysis aren't of interest.
	 */
	@Override
	public Stream<AnswerSet> solve(NormalProgram program, java.util.function.Predicate<Predicate> filter) {
		CompiledProgram preprocessed = performProgramPreprocessing(program);
		return solve(preprocessed, filter);
	}

	/**
	 * Solves the given program and filters answer sets based on the passed predicate.
	 * 
	 * @param program an {@link InternalProgram} to solve
	 * @param filter  {@link Predicate} filtering {@at.ac.tuwien.kr.alpha.common.Predicate}s in the returned answer sets
	 * @return a Stream of answer sets representing stable models of the given program
	 */
	private Stream<AnswerSet> solve(CompiledProgram program, java.util.function.Predicate<Predicate> filter) {
		Stream<AnswerSet> retVal = prepareSolverFor(program, filter).stream();
		return sortAnswerSets ? retVal.sorted() : retVal;
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
	private Solver prepareSolverFor(CompiledProgram program, java.util.function.Predicate<Predicate> filter) {
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = grounderFactory.createGrounder(program, atomStore, filter);
		return solverFactory.createSolver(grounder, atomStore);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(InputProgram program) {
		return prepareDebugSolve(program, InputConfig.DEFAULT_FILTER);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(NormalProgram program) {
		return prepareDebugSolve(program, InputConfig.DEFAULT_FILTER);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(final InputProgram program, java.util.function.Predicate<Predicate> filter) {
		return prepareDebugSolve(normalizeProgram(program), filter);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(final NormalProgram program, java.util.function.Predicate<Predicate> filter) {
		final DependencyGraph depGraph;
		final ComponentGraph compGraph;
		final AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(program);
		final NormalProgram preprocessed;
		// TODO get the StratifiedEvaluation from factory
		preprocessed = new StratifiedEvaluation(actionContext, true).transform(analyzed).toNormalProgram();
		depGraph = analyzed.getDependencyGraph();
		compGraph = analyzed.getComponentGraph();
		final Solver solver = prepareSolverFor(analyzed, filter);
		return new DebugSolvingContext() {

			@Override
			public Solver getSolver() {
				return solver;
			}

			@Override
			public NormalProgram getPreprocessedProgram() {
				return preprocessed;
			}

			@Override
			public NormalProgram getNormalizedProgram() {
				return program;
			}

			@Override
			public DependencyGraph getDependencyGraph() {
				return depGraph;
			}

			@Override
			public ComponentGraph getComponentGraph() {
				return compGraph;
			}
		};
	}

	@Override
	public Solver prepareSolverFor(InputProgram program, java.util.function.Predicate<Predicate> filter) {
		return prepareSolverFor(normalizeProgram(program), filter);
	}

	@Override
	public Solver prepareSolverFor(NormalProgram program, java.util.function.Predicate<Predicate> filter) {
		return prepareSolverFor(performProgramPreprocessing(program), filter);
	}

}
