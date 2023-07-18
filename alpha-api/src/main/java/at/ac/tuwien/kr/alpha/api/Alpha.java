package at.ac.tuwien.kr.alpha.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestResult;

/**
 * Main API entry point for the Alpha ASP system. Provides facilities for parsing, normalizing and solving ASP programs.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Alpha {

	/**
	 * Reads an ASP program using the configuration and sources specified in a given {@link InputConfig}.
	 * 
	 * @param cfg and {@link InputConfig} specifying program sources (strings, files) as well as config metadata (e.g. literate program,
	 *            external atoms, etc)
	 * @return an {@link ASPCore2Program} representing the parsed ASP code from all sources referenced in the given {@link InputConfig}
	 * @throws IOException in case one or more program sources (e.g. files) cannot be read, or parsing fails
	 */
	ASPCore2Program readProgram(InputConfig cfg) throws IOException;

	/**
	 * Reads and parses an {@link ASPCore2Program} from a list of {@link String}s representing paths.
	 * 
	 * @param literate  flag indicating whether ASP code should be treated as "literate".
	 * @param externals Custom {@link PredicateInterpretation}s for user-defined external atoms
	 * @param paths     a list of {@link String}s representing paths containing all sources from which ASP code should be read
	 * @return an {@link ASPCore2Program} representing the parsed ASP code from all given path strings
	 * @throws IOException in case one or more program sources cannot be read, or parsing fails
	 */
	ASPCore2Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException;

	/**
	 * see {@link Alpha#readProgramFiles(boolean, Map, List)}
	 */
	ASPCore2Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException;

	/**
	 * Parses a given String into an {@link ASPCore2Program}, using a map of custom {@link PredicateInterpretation}s to resolve external atoms
	 * in ASP code.
	 * 
	 * @param aspString a string representing a valid ASP-Core2 program
	 * @param externals a map of custom {@link PredicateInterpretation}s against which external atoms in the given code are resolved
	 * @return an {@link ASPCore2Program} representing the parsed ASP code
	 */
	ASPCore2Program readProgramString(String aspString, Map<String, PredicateInterpretation> externals);

	/**
	 * Convenience method to parse ASP strings not containing any user-defined external atoms, see {@link Alpha#readProgramString(String, Map)}.
	 */
	ASPCore2Program readProgramString(String aspString);

	/**
	 * Prepares a {@link DebugSolvingContext} for the given {@link ASPCore2Program} to debug program preprocessing.
	 * 
	 * @return a {@link DebugSolvingContext} holding debug information for the given program
	 */
	DebugSolvingContext prepareDebugSolve(final ASPCore2Program program);

	/**
	 * Prepares a {@link DebugSolvingContext} for the given {@link NormalProgram} to debug program preprocessing.
	 * 
	 * @return a {@link DebugSolvingContext} holding debug information for the given program
	 */
	DebugSolvingContext prepareDebugSolve(final NormalProgram program);

	/**
	 * Prepares a {@link DebugSolvingContext} for the given {@link ASPCore2Program} to debug program preprocessing.
	 * 
	 * @param filter a {@link java.util.function.Predicate} against which {@link Predicate}s of answer sets are tested.
	 * @return a {@link DebugSolvingContext} holding debug information for the given program
	 */
	DebugSolvingContext prepareDebugSolve(final ASPCore2Program program, java.util.function.Predicate<Predicate> filter);

	/**
	 * Prepares a {@link DebugSolvingContext} for the given {@link NormalProgram} to debug program preprocessing.
	 * 
	 * @param filter a {@link java.util.function.Predicate} against which {@link Predicate}s of answer sets are tested.
	 * @return a {@link DebugSolvingContext} holding debug information for the given program
	 */
	DebugSolvingContext prepareDebugSolve(final NormalProgram program, java.util.function.Predicate<Predicate> filter);

	/**
	 * Solves the given {@link ASPCore2Program}.
	 * @param program an input program
	 * @return a {@link Stream} of {@link AnswerSet}s of the given program
	 */
	Stream<AnswerSet> solve(ASPCore2Program program);

	/**
	 * Solves the given {@link ASPCore2Program}.
	 * @param program an input program
	 * @param filter a {@link java.util.function.Predicate} against which {@link Predicate}s of answer sets are tested.
	 * @return a {@link Stream} of {@link AnswerSet}s of the given program
	 */
	Stream<AnswerSet> solve(ASPCore2Program program, java.util.function.Predicate<Predicate> filter);

	/**
	 * Solves the given {@link NormalProgram}.
	 * @param program an input program
	 * @return a {@link Stream} of {@link AnswerSet}s of the given program
	 */
	Stream<AnswerSet> solve(NormalProgram program);

	/**
	 * Solves the given {@link NormalProgram}.
	 * @param program an input program
	 * @param filter a {@link java.util.function.Predicate} against which {@link Predicate}s of answer sets are tested.
	 * @return a {@link Stream} of {@link AnswerSet}s of the given program
	 */
	Stream<AnswerSet> solve(NormalProgram program, java.util.function.Predicate<Predicate> filter);

	/**
	 * Normalizes a program, i.e. rewrites all syntax constructs not natively supported by Alphas back-end into semantically equivalent ASP code.
	 * See {@link NormalProgram},
	 * @param program An {@link ASPCore2Program} to normalize
	 * @return a {@link NormalProgram} that is a semantic equivalent to the given input program
	 */
	NormalProgram normalizeProgram(ASPCore2Program program);

	/**
	 * Constructs a @{link Solver} pre-loaded with the given {@link ASPCore2Program} from which {@link AnswerSet}s can be obtained via {@link Solver#stream()}.
	 * 
	 * @param program the program to solve
	 * @param filter a {@link java.util.function.Predicate} against which {@link Predicate}s of answer sets are tested.
	 * @return a {@link Solver} pre-loaded withthe given program
	 */
	Solver prepareSolverFor(ASPCore2Program program, java.util.function.Predicate<Predicate> filter);

	/**
	 * Constructs a @{link Solver} pre-loaded with the given {@link NormalProgram} from which {@link AnswerSet}s can be obtained via {@link Solver#stream()}.
	 * 
	 * @param program the program to solve
	 * @param filter a {@link java.util.function.Predicate} against which {@link Predicate}s of answer sets are tested.
	 * @return a {@link Solver} pre-loaded withthe given program
	 */
	Solver prepareSolverFor(NormalProgram program, java.util.function.Predicate<Predicate> filter);

	/**
	 * Reifies, i.e. re-expresses as a set of ASP facts, the given input program.
	 * 
	 * @param program an ASP program to reify
	 * @return a set of {@link BasicAtom}s encoding the given program
	 */
	Set<BasicAtom> reify(ASPCore2Program program);

	/**
	 * Runs all test cases of the given program.
	 */
	TestResult test(ASPCore2Program program);

}
