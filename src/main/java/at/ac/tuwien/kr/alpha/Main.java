/**
 * Copyright (c) 2016-2017, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main entry point for Alpha.
 */
public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

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

	private static final String OPT_BRANCHING_HEURISTIC = "branchingHeuristic";
	private static final String DEFAULT_GROUNDER = "naive";
	private static final String DEFAULT_SOLVER = "default";
	private static final String DEFAULT_STORE = "alphaRoaming";
	private static final String OPT_SEED = "seed";
	private static final String OPT_DEBUG_INTERNAL_CHECKS = "DebugEnableInternalChecks";
	private static final String DEFAULT_BRANCHING_HEURISTIC = Heuristic.NAIVE.name();

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

		Option quietOption = new Option("q", OPT_QUIET, false, "do not print answer sets");
		options.addOption(quietOption);

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

		java.util.function.Predicate<Predicate> filter = p -> true;

		if (commandLine.hasOption(OPT_FILTER)) {
			Set<String> desiredPredicates = new HashSet<>(Arrays.asList(commandLine.getOptionValues(OPT_FILTER)));
			filter = p -> desiredPredicates.contains(p.getPredicateName());
		}

		Bridge[] bridges = new Bridge[0];

		int limit = 0;

		try {
			Number n = (Number)commandLine.getParsedOptionValue(OPT_NUM_AS);
			if (n != null) {
				limit = n.intValue();
			}
		} catch (ParseException e) {
			bailOut("Failed to parse number of answer sets requested.", e);
		}

		boolean debugInternalChecks = commandLine.hasOption(OPT_DEBUG_INTERNAL_CHECKS);

		Program program = null;
		try {
			ProgramParser parser = new ProgramParser();

			if (commandLine.hasOption(OPT_STRING)) {
				program = parser.parse(commandLine.getOptionValue(OPT_STRING));
			} else {
				// Parse all input files and accumulate their results in one Program.
				String[] inputFileNames = commandLine.getOptionValues(OPT_INPUT);
				program = parser.parse(CharStreams.fromFileName(inputFileNames[0]));

				for (int i = 1; i < inputFileNames.length; i++) {
					program.accumulate(parser.parse(CharStreams.fromFileName(inputFileNames[i])));
				}
			}
		} catch (RecognitionException e) {
			// In case a recognitionexception occured, parseVisit will
			// already have printed an error message, so we just exit
			// at this point without further logging.
			System.exit(1);
		} catch (FileNotFoundException e) {
			bailOut(e.getMessage());
		} catch (IOException e) {
			bailOut("Failed to parse program.", e);
		}

		Grounder grounder = GrounderFactory.getInstance(
			commandLine.getOptionValue(OPT_GROUNDER, DEFAULT_GROUNDER), program, filter, bridges
		);

		// NOTE: Using time as seed is fine as the internal heuristics
		// do not need to by cryptographically securely randomized.
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

		String chosenSolver = commandLine.getOptionValue(OPT_SOLVER, DEFAULT_SOLVER);
		String chosenStore = commandLine.getOptionValue(OPT_STORE, DEFAULT_STORE);
		String chosenBranchingHeuristic = commandLine.getOptionValue(OPT_BRANCHING_HEURISTIC, DEFAULT_BRANCHING_HEURISTIC);
		Heuristic parsedChosenBranchingHeuristic = null;
		try {
			parsedChosenBranchingHeuristic = Heuristic.valueOf(chosenBranchingHeuristic.replace("-", "_").toUpperCase());
		} catch (IllegalArgumentException e) {
			bailOut("Unknown branching heuristic: {}. Please try one of the following: {}.", chosenBranchingHeuristic, Heuristic.listAllowedValues());
		}

		Solver solver = SolverFactory.getInstance(
			chosenSolver, chosenStore, grounder, new Random(seed), parsedChosenBranchingHeuristic, debugInternalChecks
		);

		Stream<AnswerSet> stream = solver.stream();

		if (limit > 0) {
			stream = stream.limit(limit);
		}

		if (commandLine.hasOption(OPT_SORT)) {
			stream = stream.sorted();
		}

		if (!commandLine.hasOption(OPT_QUIET)) {
			stream.forEach(System.out::println);
		} else {
			// Note: Even though we are not consuming the result, we will still compute answer sets.
			stream.collect(Collectors.toList());
		}
	}

	private static void exitWithHelp(Options options, int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		// TODO(flowlo): This is quite optimistic. How do we know that the program
		// really was invoked as "java -jar ..."?
		formatter.printHelp("java -jar alpha-bundled.jar" + System.lineSeparator() + "java -jar alpha.jar", options);
		System.exit(exitCode);
	}

	private static void bailOut(String format, Object... arguments) {
		LOGGER.error(format, arguments);
		System.exit(1);
	}
}
