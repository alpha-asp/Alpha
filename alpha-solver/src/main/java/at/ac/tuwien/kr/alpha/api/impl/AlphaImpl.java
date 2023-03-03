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

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.DebugSolvingContext;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestResult;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.Programs.ASPCore2ProgramBuilder;
import at.ac.tuwien.kr.alpha.commons.programs.reification.Reifier;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.programs.tests.Tests;
import at.ac.tuwien.kr.alpha.commons.util.IdGenerator;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;
import at.ac.tuwien.kr.alpha.commons.util.Util;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlphaImpl implements Alpha {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlphaImpl.class);

	private final SystemConfig config; // Config is initialized with default values.
	private final ProgramParser parser = new ProgramParserImpl();
	private final Reifier reifier = new Reifier(() -> {
		IdGenerator<Integer> idGen = new IntIdGenerator(0);
		return () -> Terms.newConstant(idGen.getNextId());
	});


	public AlphaImpl(SystemConfig cfg) {
		this.config = cfg;
	}

	public AlphaImpl() {
		this.config = new SystemConfig();
	}

	@Override
	public ASPCore2Program readProgram(InputConfig cfg) throws IOException {
		ASPCore2ProgramBuilder prgBuilder = Programs.builder();
		ASPCore2Program tmpProg;
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
	public ASPCore2Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException {
		return readProgramFiles(literate, externals, paths.stream().map(Paths::get).collect(Collectors.toList()).toArray(new Path[] {}));
	}

	@Override
	public ASPCore2Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException {
		ASPCore2ProgramBuilder prgBuilder = Programs.builder();
		ASPCore2Program tmpProg;
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
	public ASPCore2Program readProgramString(String aspString, Map<String, PredicateInterpretation> externals) {
		return parser.parse(aspString, externals);
	}

	@Override
	public ASPCore2Program readProgramString(String aspString) {
		return readProgramString(aspString, Collections.emptyMap());
	}

	@Override
	public NormalProgram normalizeProgram(ASPCore2Program program) {
		return new NormalizeProgramTransformation(config.getAggregateRewritingConfig()).apply(program);
	}

	@VisibleForTesting
	InternalProgram performProgramPreprocessing(NormalProgram program) {
		LOGGER.debug("Preprocessing InternalProgram!");
		InternalProgram retVal = InternalProgram.fromNormalProgram(program);
		if (config.isEvaluateStratifiedPart()) {
			AnalyzedProgram analyzed = new AnalyzedProgram(retVal.getRules(), retVal.getFacts());
			retVal = new StratifiedEvaluation().apply(analyzed);
		}
		return retVal;
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}) for cases where details of the
	 * program analysis and normalization aren't of interest.
	 */
	@Override
	public Stream<AnswerSet> solve(ASPCore2Program program) {
		return solve(program, InputConfig.DEFAULT_FILTER);
	}

	/**
	 * Convenience method - overloaded version of solve({@link InternalProgram}, {@link Predicate}) for cases where
	 * details of the program analysis and normalization aren't of interest.
	 */
	@Override
	public Stream<AnswerSet> solve(ASPCore2Program program, java.util.function.Predicate<Predicate> filter) {
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
	private Solver prepareSolverFor(CompiledProgram program, java.util.function.Predicate<Predicate> filter) {
		String grounderName = config.getGrounderName();
		boolean doDebugChecks = config.isDebugInternalChecks();

		GrounderHeuristicsConfiguration grounderHeuristicConfiguration = GrounderHeuristicsConfiguration
				.getInstance(config.getGrounderToleranceConstraints(), config.getGrounderToleranceRules());
		grounderHeuristicConfiguration.setAccumulatorEnabled(config.isGrounderAccumulatorEnabled());

		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance(grounderName, program, atomStore, filter, grounderHeuristicConfiguration, doDebugChecks);

		return SolverFactory.getInstance(config, atomStore, grounder);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(ASPCore2Program program) {
		return prepareDebugSolve(program, InputConfig.DEFAULT_FILTER);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(NormalProgram program) {
		return prepareDebugSolve(program, InputConfig.DEFAULT_FILTER);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(final ASPCore2Program program, java.util.function.Predicate<Predicate> filter) {
		return prepareDebugSolve(normalizeProgram(program), filter);
	}

	@Override
	public DebugSolvingContext prepareDebugSolve(final NormalProgram program, java.util.function.Predicate<Predicate> filter) {
		final DependencyGraph depGraph;
		final ComponentGraph compGraph;
		final AnalyzedProgram analyzed = AnalyzedProgram.analyzeNormalProgram(program);
		final NormalProgram preprocessed;
		if (this.config.isEvaluateStratifiedPart()) {
			preprocessed = new StratifiedEvaluation().apply(analyzed).toNormalProgram();
		} else {
			preprocessed = program;
		}
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
	public Solver prepareSolverFor(ASPCore2Program program, java.util.function.Predicate<Predicate> filter) {
		return prepareSolverFor(normalizeProgram(program), filter);
	}

	@Override
	public Solver prepareSolverFor(NormalProgram program, java.util.function.Predicate<Predicate> filter) {
		return prepareSolverFor(performProgramPreprocessing(program), filter);
	}

	@Override
	public Set<BasicAtom> reify(ASPCore2Program program) {
		return reifier.reifyProgram(program);
	}

	public TestResult test(ASPCore2Program program) {
		NormalProgram programUnderTest = normalizeProgram(program);
		for (TestCase testCase : program.getTestCases()) {
			runTestCase(programUnderTest, testCase, true); // TODO
		}
		return null; // TODO
	}

	private TestResult.TestCaseResult runTestCase(NormalProgram programUnderTest, TestCase testCase, boolean skipOnFailure) {
		List<Atom> facts = new ArrayList<>(programUnderTest.getFacts());
		facts.addAll(testCase.getInput());
		NormalProgram prog = Programs.newNormalProgram(programUnderTest.getRules(), facts, programUnderTest.getInlineDirectives());
		Set<AnswerSet> answerSets = solve(prog).collect(Collectors.toSet());
		IntPredicate answerSetsVerifier = testCase.getAnswerSetCountVerifier();
		Optional<String> answerSetCountErrMsg;
		if (!answerSetsVerifier.test(answerSets.size())) {
			answerSetCountErrMsg = Optional.of("Answer Set count incorrect, verifier: " + answerSetsVerifier + " failed, count is " + answerSets.size());
			if (skipOnFailure) {
				return Tests.newTestCaseResult(testCase.getName(), answerSetCountErrMsg, 0, 0, testCase.getAssertions().size(), Collections.emptyMap());
			}
		}
		int passedCnt = 0;
		int failedCnt = 0;
		Map<Assertion, List<String>> assertionErrors = new LinkedHashMap<>();
		for (Assertion assertion : testCase.getAssertions()) {
			java.util.function.Predicate<AnswerSet> assertionMatcher = (as) -> this.answerSetSatisfiesAssertion(as, assertion);
			List<String> errorList = assertionErrors.computeIfAbsent(assertion, (ass) -> new ArrayList<>());
			switch (assertion.getMode()) {
				case FOR_ALL:
					if (!answerSets.stream().allMatch(assertionMatcher)) {
						errorList.addAll(
								answerSets.stream()
										.filter(assertionMatcher.negate())
										.map((as) -> "Universal assertion failed on answer set: " + as)
										.collect(Collectors.toSet())
						);
					}
					break;
				case FOR_SOME:
					if (answerSets.stream().noneMatch(assertionMatcher)) {
						errorList.add("No answer set matches existential assertion!");
					}
					break;
				default:
					throw new UnsupportedOperationException("Unsupported assertion mode: " + assertion.getMode());
			}
		}
		return null; // TODO
	}

	private boolean answerSetSatisfiesAssertion(AnswerSet as, Assertion assertion) {
		ASPCore2Program verifierWithInput = Programs.builder(assertion.getVerifier()).addFacts(new ArrayList<>(as.asFacts())).build();
		return solve(verifierWithInput).findAny().isPresent();
	}



}
