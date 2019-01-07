/**
 * Copyright (c) 2016-2018, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.SolverMaintainingStatistics;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfigurationBuilder;
import at.ac.tuwien.kr.alpha.solver.heuristics.MOMs;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.ac.tuwien.kr.alpha.Util.literate;
import static at.ac.tuwien.kr.alpha.Util.streamToChannel;
import static java.nio.file.Files.lines;

/**
 * Main entry point for Alpha.
 */
public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private static final ProgramParser PARSER = new ProgramParser();

	private static final String OPT_INPUT = "input";
	private static final String OPT_HELP  = "help";
	private static final String OPT_NUM_AS  = "numAS";
	private static final String OPT_GROUNDER = "grounder";
	private static final String OPT_SOLVER = "solver";
	private static final String OPT_FILTER = "filter";
	private static final String OPT_STRING = "str";
	private static final String OPT_SORT = "sort";
	private static final String OPT_DETERMINISTIC = "deterministic";
	private static final String OPT_STORE = "store";
	private static final String OPT_QUIET = "quiet";
	private static final String OPT_LITERATE = "literate";
	private static final String OPT_STATS = "stats";
	private static final String OPT_NO_JUSTIFICATION = "disableJustifications";
	private static final String OPT_NORMALIZATION_GRID = "normalizationCountingGrid";

	private static final String OPT_BRANCHING_HEURISTIC = "branchingHeuristic";
	private static final String DEFAULT_BRANCHING_HEURISTIC = Heuristic.NAIVE.name();
	
	private static final String OPT_MOMS_STRATEGY = "momsStrategy";
	private static final String DEFAULT_MOMS_STRATEGY = MOMs.Strategy.CountBinaryWatches.name();
	
	private static final String DEFAULT_GROUNDER = "naive";
	private static final String DEFAULT_SOLVER = "default";
	private static final String DEFAULT_STORE = "alphaRoaming";
	private static final String OPT_SEED = "seed";
	private static final String OPT_DEBUG_INTERNAL_CHECKS = "DebugEnableInternalChecks";

	private static final java.util.function.Predicate<Predicate> DEFAULT_FILTER = p -> true;

	private static CommandLine commandLine;

	public static void main(String[] args) {
		final Options options = new Options();

		Option numAnswerSetsOption = new Option("n", OPT_NUM_AS, true, "the number of Answer Sets to compute");
		numAnswerSetsOption.setArgName("number");
		numAnswerSetsOption.setRequired(false);
		numAnswerSetsOption.setArgs(1);
		numAnswerSetsOption.setType(Number.class);
		options.addOption(numAnswerSetsOption);

		Option inputOption = new Option("i", OPT_INPUT, true, "read the ASP program from this file");
		inputOption.setArgName("file");
		inputOption.setArgs(1);
		inputOption.setType(FileInputStream.class);
		options.addOption(inputOption);

		Option helpOption = new Option("h", OPT_HELP, false, "show this help");
		options.addOption(helpOption);

		Option grounderOption = new Option("g", OPT_GROUNDER, false, "name of the grounder implementation to use");
		grounderOption.setArgs(1);
		grounderOption.setArgName("grounder");
		options.addOption(grounderOption);

		Option solverOption = new Option("s", OPT_SOLVER, false, "name of the solver implementation to use");
		solverOption.setArgs(1);
		solverOption.setArgName("solver");
		options.addOption(solverOption);

		Option storeOption = new Option("r", OPT_STORE, false, "name of the nogood store implementation to use");
		storeOption.setArgs(1);
		storeOption.setArgName("store");
		options.addOption(storeOption);

		Option filterOption = new Option("f", OPT_FILTER, true, "predicates to show when printing answer sets");
		filterOption.setArgs(1);
		filterOption.setArgName("filter");
		filterOption.setValueSeparator(',');
		options.addOption(filterOption);

		Option strOption = new Option("str", OPT_STRING, true, "provide the ASP program in form of a string");
		inputOption.setArgs(1);
		inputOption.setArgName("string");
		inputOption.setType(String.class);
		options.addOption(strOption);

		Option sortOption = new Option("sort", OPT_SORT, false, "sort answer sets");
		options.addOption(sortOption);

		Option deterministicOption = new Option("d", OPT_DETERMINISTIC, false, "disable randomness");
		options.addOption(deterministicOption);

		Option seedOption = new Option("e", OPT_SEED, true, "set seed");
		seedOption.setArgName("number");
		seedOption.setRequired(false);
		seedOption.setArgs(1);
		seedOption.setType(Number.class);
		options.addOption(seedOption);

		Option debugFlags = new Option(OPT_DEBUG_INTERNAL_CHECKS, "run additional (time-consuming) safety checks.");
		options.addOption(debugFlags);

		Option branchingHeuristicOption = new Option("b", OPT_BRANCHING_HEURISTIC, false, "name of the branching heuristic to use");
		branchingHeuristicOption.setArgs(1);
		branchingHeuristicOption.setArgName("heuristic");
		options.addOption(branchingHeuristicOption);
		
		Option momsStrategyOption = new Option("ms", OPT_MOMS_STRATEGY, false, "strategy for mom's heuristic (CountBinaryWatches or BinaryNoGoodPropagation)");
		momsStrategyOption.setArgs(1);
		momsStrategyOption.setArgName("strategy");
		options.addOption(momsStrategyOption);

		Option quietOption = new Option("q", OPT_QUIET, false, "do not print answer sets");
		options.addOption(quietOption);

		Option literateOption = new Option("l", OPT_LITERATE, false, "enable literate programming mode");
		options.addOption(literateOption);

		Option statsOption = new Option("st", OPT_STATS, false, "print statistics");
		options.addOption(statsOption);

		Option justificationOption = new Option(OPT_NO_JUSTIFICATION, "disable the search for justifications on must-be-true assigned atoms in the solver.");
		options.addOption(justificationOption);

		Option normalizationCountingGrid = new Option(OPT_NORMALIZATION_GRID, "ues counting grid normalization instead of sorting circuit for #count.");
		options.addOption(normalizationCountingGrid);

		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			exitWithHelp(options, 1);
			return;
		}

		if (commandLine.hasOption(OPT_HELP)) {
			exitWithHelp(options, 0);
			return;
		}

		if (!commandLine.hasOption(OPT_STRING) && !commandLine.hasOption(OPT_INPUT)) {
			System.err.println("No input program is specified. Aborting.");
			exitWithHelp(options, 1);
			return;
		}

		java.util.function.Predicate<Predicate> filter = DEFAULT_FILTER;
		if (commandLine.hasOption(OPT_FILTER)) {
			Set<String> desiredPredicates = new HashSet<>(Arrays.asList(commandLine.getOptionValues(OPT_FILTER)));
			filter = p -> desiredPredicates.contains(p.getName());
		}

		final boolean disableJustifications = commandLine.hasOption(OPT_NO_JUSTIFICATION);
		final boolean normalizationUseGrid = commandLine.hasOption(OPT_NORMALIZATION_GRID);
		final boolean debugInternalChecks = commandLine.hasOption(OPT_DEBUG_INTERNAL_CHECKS);
		final boolean literate = commandLine.hasOption(OPT_LITERATE);

		Program program = null;
		try {
			if (commandLine.hasOption(OPT_STRING)) {
				program = PARSER.parse(commandLine.getOptionValue(OPT_STRING));
			} else {
				// Parse all input files and accumulate their results in one Program.
				program = combineInput(literate, commandLine.getOptionValues(OPT_INPUT));
			}
		} catch (RecognitionException e) {
			// In case a recognition exception occurred, parseVisit will
			// already have printed an error message, so we just exit
			// at this point without further logging.
			System.exit(1);
		} catch (FileNotFoundException e) {
			bailOut(e.getMessage());
		} catch (IOException e) {
			bailOut("Failed to parse program.", e);
		}

		final AtomStore atomStore = new AtomStoreImpl();
		final Grounder grounder = GrounderFactory.getInstance(
			commandLine.getOptionValue(OPT_GROUNDER, DEFAULT_GROUNDER), program, atomStore, filter, normalizationUseGrid, debugInternalChecks
		);

		// NOTE: Using time as seed is fine as the internal heuristics
		// do not need to be cryptographically securely randomized.
		long seed = commandLine.hasOption(OPT_DETERMINISTIC) ? 0 : System.nanoTime();

		try {
			Number s = (Number)commandLine.getParsedOptionValue(OPT_SEED);
			if (s != null) {
				seed = s.longValue();
			}
		} catch (ParseException e) {
			bailOut("Failed to parse seed.", e);
		}

		LOGGER.info("Seed for pseudorandomization is {}.", seed);

		HeuristicsConfigurationBuilder heuristicsConfigurationBuilder = HeuristicsConfiguration.builder();
		
		final String chosenBranchingHeuristic = commandLine.getOptionValue(OPT_BRANCHING_HEURISTIC, DEFAULT_BRANCHING_HEURISTIC);
		try {
			heuristicsConfigurationBuilder.setHeuristic(Heuristic.valueOf(chosenBranchingHeuristic.replace("-", "_").toUpperCase()));
		} catch (IllegalArgumentException e) {
			bailOut("Unknown branching heuristic: {}. Please try one of the following: {}.", chosenBranchingHeuristic, Heuristic.listAllowedValues());
		}
		
		final String chosenMomsStrategy = commandLine.getOptionValue(OPT_MOMS_STRATEGY, DEFAULT_MOMS_STRATEGY);
		try {
			heuristicsConfigurationBuilder.setMomsStrategy(MOMs.Strategy.valueOf(chosenMomsStrategy));
		} catch (IllegalArgumentException e) {
			bailOut("Unknown mom's strategy: {}. Please try one of the following: {}.", chosenMomsStrategy, MOMs.Strategy.listAllowedValues());
		}

		final String chosenSolver = commandLine.getOptionValue(OPT_SOLVER, DEFAULT_SOLVER);
		final String chosenStore = commandLine.getOptionValue(OPT_STORE, DEFAULT_STORE);
		Solver solver = SolverFactory.getInstance(
			chosenSolver, chosenStore, atomStore, grounder, new Random(seed), heuristicsConfigurationBuilder.build(), debugInternalChecks, disableJustifications
		);

		computeAndConsumeAnswerSets(solver);
	}

	private static void computeAndConsumeAnswerSets(Solver solver) {
		Stream<AnswerSet> stream = solver.stream();

		int limit = getRequestedNumberOfAnswerSets();
		if (limit > 0) {
			stream = stream.limit(limit);
		}

		if (commandLine.hasOption(OPT_SORT)) {
			stream = stream.sorted();
		}

		if (!commandLine.hasOption(OPT_QUIET)) {
			AtomicInteger counter = new AtomicInteger(0);
			stream.forEach(as -> System.out.println("Answer set " + counter.incrementAndGet() + ":" + System.lineSeparator() + as.toString()));
			if (counter.get() == 0) {
				System.out.println("UNSATISFIABLE");
			} else {
				System.out.println("SATISFIABLE");
			}
		} else {
			// Note: Even though we are not consuming the result, we will still compute answer sets.
			stream.collect(Collectors.toList());
		}
		printStatisticsIfEnabled(solver);
	}

	static int getRequestedNumberOfAnswerSets() {
		int limit = 0;

		try {
			Number n = (Number) commandLine.getParsedOptionValue(OPT_NUM_AS);
			if (n != null) {
				limit = n.intValue();
			}
		} catch (ParseException e) {
			bailOut("Failed to parse number of answer sets requested.", e);
		}

		return limit;
	}

	private static void printStatisticsIfEnabled(Solver solver) {
		if (commandLine.hasOption(OPT_STATS) && solver instanceof SolverMaintainingStatistics) {
			((SolverMaintainingStatistics) solver).printStatistics();
		}
	}

	private static void exitWithHelp(Options options, int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		// TODO(lorenzleutgeb): This is quite optimistic. How do we know that the program
		// really was invoked as "java -jar ..."?
		formatter.printHelp("java -jar alpha-bundled.jar" + System.lineSeparator() + "java -jar alpha.jar", options);
		System.exit(exitCode);
	}

	private static void bailOut(String format, Object... arguments) {
		LOGGER.error(format, arguments);
		System.exit(1);
	}

	private static Program combineInput(boolean literate, String... fileNames) throws IOException {
		final Program result = new Program();

		for (String fileName : fileNames) {
			CharStream stream;
			if (!literate) {
				stream = CharStreams.fromFileName(fileName);
			} else {
				stream = CharStreams.fromChannel(
					streamToChannel(literate(lines(Paths.get(fileName)))),
					4096,
					CodingErrorAction.REPLACE,
					fileName
				);
			}
			result.accumulate(PARSER.parse(stream));
		}

		return result;
	}
}
