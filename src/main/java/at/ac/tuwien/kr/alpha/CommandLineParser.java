package at.ac.tuwien.kr.alpha;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineParser.class);

	private static final Option OPT_NUM_ANSWER_SETS = Option.builder("n").longOpt("numAS").hasArg(true).argName("number").type(Integer.class)
			.desc("the number of answer sets to compute").build();
	private static final Option OPT_HELP = Option.builder("h").longOpt("help").hasArg(false).desc("shows this help").build();
	private static final Option OPT_INPUT = Option.builder("i").longOpt("input").hasArg(true).argName("file").type(FileInputStream.class)
			.desc("read ASP program from this file").build();
	private static final Option OPT_GROUNDER = Option.builder("g").longOpt("grounder").hasArg(true).argName("grounder")
			.desc("the grounder implementation to use").build();
	private static final Option OPT_SOLVER = Option.builder("s").longOpt("solver").hasArg(true).argName("solver").desc("the solver implementation to use")
			.build();
	private static final Option OPT_NOGOOD_STORE = Option.builder("r").longOpt("store").hasArg(true).argName("store").desc("the nogood store to use").build();
	private static final Option OPT_FILTER = Option.builder("f").longOpt("filter").hasArg(true).argName("filter").valueSeparator(',')
			.desc("predicates to show when printing answer sets").build();
	private static final Option OPT_ASPSTRING = Option.builder("str").longOpt("aspstring").hasArg(true).argName("program").type(String.class)
			.desc("provide the asp program as a string").build();
	private static final Option OPT_SORT = Option.builder("sort").longOpt("sort").hasArg(false).desc("sort answer sets").build();
	private static final Option OPT_DETERMINISTIC = Option.builder("d").longOpt("deterministic").hasArg(false).desc("disables randomness").build();
	private static final Option OPT_SEED = Option.builder("e").longOpt("seed").hasArg(true).argName("seed").type(Integer.class).desc("set seed").build();
	private static final Option OPT_DEBUG_INTERNAL_CHECKS = Option.builder("dbg").longOpt("DebugEnableInternalChecks")
			.desc("run additional (time-consuming) safety checks").build();
	private static final Option OPT_BRANCHING_HEURISTIC = Option.builder("b").longOpt("branchingHeuristic").hasArg(true).argName("heuristic")
			.desc("the branching heuristic to use").build();
	private static final Option OPT_QUIET = Option.builder("q").longOpt("quiet").desc("do not print answer sets").build();
	private static final Option OPT_LITERATE = Option.builder("l").longOpt("literate").desc("enable literate programming mode").build();
	private static final Option OPT_STATS = Option.builder("st").longOpt("stats").desc("print statistics").build();
	private static final Option OPT_NO_JUSTIFICATION = Option.builder("dj").longOpt("disableJustifications")
			.desc("disable the search for justifications on must-be-true assigned atoms in the solver").build();
	private static final Option OPT_NORMALIZATION_GRID = Option.builder("ng").longOpt("normalizationCountingGrid")
			.desc("use counting grid normalization instead of sorting circuit for #count").build();

	private static final Options CLI_OPTS = new Options();
	private static final Map<String, IOptionHandler> OPT_HANDLERS = new HashMap<>();

	static {
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NUM_ANSWER_SETS);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_NUM_ANSWER_SETS.getOpt(), CommandLineParser::handleNumAnswerSets);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_HELP);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_HELP.getOpt(), CommandLineParser::handleHelp);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_INPUT);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_INPUT.getOpt(), CommandLineParser::handleInput);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_GROUNDER);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_GROUNDER.getOpt(), CommandLineParser::handleGrounder);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SOLVER);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_SOLVER.getOpt(), CommandLineParser::handleSolver);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NOGOOD_STORE);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_NOGOOD_STORE.getOpt(), CommandLineParser::handleNogoodStore);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_FILTER);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_FILTER.getOpt(), CommandLineParser::handleFilters);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_ASPSTRING);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_ASPSTRING.getOpt(), CommandLineParser::handleAspString);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SORT);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_SORT.getOpt(), CommandLineParser::handleSort);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_DETERMINISTIC);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_DETERMINISTIC.getOpt(), CommandLineParser::handleDeterministic);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SEED);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_SEED.getOpt(), CommandLineParser::handleSeed);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_DEBUG_INTERNAL_CHECKS);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_DEBUG_INTERNAL_CHECKS.getOpt(), CommandLineParser::handleInternalChecks);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_BRANCHING_HEURISTIC);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_BRANCHING_HEURISTIC.getOpt(), CommandLineParser::handleBranchingHeuristic);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_QUIET);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_QUIET.getOpt(), CommandLineParser::handleQuiet);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_LITERATE);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_LITERATE.getOpt(), CommandLineParser::handleLiterate);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_STATS);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_STATS.getOpt(), CommandLineParser::handleStats);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NO_JUSTIFICATION);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_NO_JUSTIFICATION.getOpt(), CommandLineParser::handleNoJustification);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NORMALIZATION_GRID);
		CommandLineParser.OPT_HANDLERS.put(CommandLineParser.OPT_NORMALIZATION_GRID.getOpt(), CommandLineParser::handleNormalizationGrid);
	}

	public AlphaConfig parseCommandLine(String[] args) throws ParseException {
		CommandLine commandLine = new DefaultParser().parse(CommandLineParser.CLI_OPTS, args);

		AlphaConfig retVal = new AlphaConfig();
		return retVal;
	}

	private static void handleNumAnswerSets(Option opt, AlphaConfig cfg) {
		
	}

	private static void handleHelp(Option opt, AlphaConfig cfg) {
		
	}

	private static void handleInput(Option opt, AlphaConfig cfg) {

	}

	private static void handleGrounder(Option opt, AlphaConfig cfg) {

	}

	private static void handleSolver(Option opt, AlphaConfig cfg) {

	}

	private static void handleNogoodStore(Option opt, AlphaConfig cfg) {

	}

	private static void handleFilters(Option opt, AlphaConfig cfg) {

	}

	private static void handleAspString(Option opt, AlphaConfig cfg) {

	}

	private static void handleSort(Option opt, AlphaConfig cfg) {

	}

	private static void handleDeterministic(Option opt, AlphaConfig cfg) {

	}

	private static void handleSeed(Option opt, AlphaConfig cfg) {

	}

	private static void handleInternalChecks(Option opt, AlphaConfig cfg) {

	}

	private static void handleBranchingHeuristic(Option opt, AlphaConfig cfg) {

	}

	private static void handleQuiet(Option opt, AlphaConfig cfg) {

	}

	private static void handleLiterate(Option opt, AlphaConfig cfg) {

	}

	private static void handleStats(Option opt, AlphaConfig cfg) {

	}

	private static void handleNoJustification(Option opt, AlphaConfig cfg) {

	}

	private static void handleNormalizationGrid(Option opt, AlphaConfig cfg) {

	}

}
