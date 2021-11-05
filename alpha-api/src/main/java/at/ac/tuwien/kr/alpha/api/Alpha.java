package at.ac.tuwien.kr.alpha.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;

/**
 * Main API entry point for the Alpha ASP system. Provides facalitites for parsing, normalizing and solving ASP programs.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Alpha {

	/**
	 * Reads an ASP program using the configuration and sources specified in a given {@link InputConfig}.
	 * 
	 * @param cfg and {@link InputConfig} specifiying program sources (strings, files) as well as config metadata (e.g. literate program,
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

	Stream<AnswerSet> solve(NormalProgram program);

	Stream<AnswerSet> solve(NormalProgram program, java.util.function.Predicate<Predicate> filter);

	NormalProgram normalizeProgram(ASPCore2Program program);

	SystemConfig getConfig();

	Solver prepareSolverFor(ASPCore2Program program, java.util.function.Predicate<Predicate> filter);

	Solver prepareSolverFor(NormalProgram program, java.util.function.Predicate<Predicate> filter);

}
