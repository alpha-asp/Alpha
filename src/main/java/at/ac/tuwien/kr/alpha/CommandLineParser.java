package at.ac.tuwien.kr.alpha;

import java.io.FileInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineParser {

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

	static {
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NUM_ANSWER_SETS);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_HELP);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_INPUT);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_GROUNDER);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SOLVER);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NOGOOD_STORE);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_FILTER);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_ASPSTRING);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SORT);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_DETERMINISTIC);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SEED);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_DEBUG_INTERNAL_CHECKS);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_BRANCHING_HEURISTIC);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_QUIET);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_LITERATE);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_STATS);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NO_JUSTIFICATION);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NORMALIZATION_GRID);
	}
	
	public AlphaConfig parseCommandLine(String[] args) throws ParseException {
		CommandLine commandLine = new DefaultParser().parse(CommandLineParser.CLI_OPTS, args);
		
		AlphaConfig retVal = new AlphaConfig();
		return retVal;
	}
}
