package at.ac.tuwien.kr.alpha;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
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

	private final Map<String, IOptionHandler> optionHandlers = new HashMap<>();
	private Consumer<String> abortAction;
	private String cmdSyntax;

	/**
	 * Creates a new <code>CommandLineParser</code>. The abortAction described below
	 * is passed into the constructor externally in order to avoid strongly coupling
	 * this class to any other part of the application. Especially an abortAction -
	 * which likely will include some call like System.exit({state}) - should not be
	 * specified by utility classes themselves, but rather by the application's main
	 * class.
	 * 
	 * @param cmdLineSyntax a string describing the basic call syntax for the
	 *                      application binary, e.g. "java -jar somejar.jar"
	 * @param abortAction   a <code>Consumer<String></code> that is called when
	 *                      option parsing is aborted, as is the case when the
	 *                      "help" option is encountered. It expects a string
	 *                      parameter, which is a message accompanying the abort
	 */
	public CommandLineParser(String cmdLineSyntax, Consumer<String> abortAction) {
		this.cmdSyntax = cmdLineSyntax;
		this.abortAction = abortAction;

		this.optionHandlers.put(CommandLineParser.OPT_NUM_ANSWER_SETS.getOpt(), this::handleNumAnswerSets);
		this.optionHandlers.put(CommandLineParser.OPT_HELP.getOpt(), this::handleHelp);
		this.optionHandlers.put(CommandLineParser.OPT_INPUT.getOpt(), this::handleInput);
		this.optionHandlers.put(CommandLineParser.OPT_GROUNDER.getOpt(), this::handleGrounder);
		this.optionHandlers.put(CommandLineParser.OPT_SOLVER.getOpt(), this::handleSolver);
		this.optionHandlers.put(CommandLineParser.OPT_NOGOOD_STORE.getOpt(), this::handleNogoodStore);
		this.optionHandlers.put(CommandLineParser.OPT_FILTER.getOpt(), this::handleFilters);
		this.optionHandlers.put(CommandLineParser.OPT_ASPSTRING.getOpt(), this::handleAspString);
		this.optionHandlers.put(CommandLineParser.OPT_SORT.getOpt(), this::handleSort);
		this.optionHandlers.put(CommandLineParser.OPT_DETERMINISTIC.getOpt(), this::handleDeterministic);
		this.optionHandlers.put(CommandLineParser.OPT_SEED.getOpt(), this::handleSeed);
		this.optionHandlers.put(CommandLineParser.OPT_DEBUG_INTERNAL_CHECKS.getOpt(), this::handleInternalChecks);
		this.optionHandlers.put(CommandLineParser.OPT_BRANCHING_HEURISTIC.getOpt(), this::handleBranchingHeuristic);
		this.optionHandlers.put(CommandLineParser.OPT_QUIET.getOpt(), this::handleQuiet);
		this.optionHandlers.put(CommandLineParser.OPT_LITERATE.getOpt(), this::handleLiterate);
		this.optionHandlers.put(CommandLineParser.OPT_STATS.getOpt(), this::handleStats);
		this.optionHandlers.put(CommandLineParser.OPT_NO_JUSTIFICATION.getOpt(), this::handleNoJustification);
		this.optionHandlers.put(CommandLineParser.OPT_NORMALIZATION_GRID.getOpt(), this::handleNormalizationGrid);
	}

	public AlphaConfig parseCommandLine(String[] args) throws ParseException {
		CommandLine commandLine = new DefaultParser().parse(CommandLineParser.CLI_OPTS, args);
		AlphaConfig retVal = new AlphaConfig();
		IOptionHandler currOptionHandler;
		for (Option opt : commandLine.getOptions()) {
			currOptionHandler = this.optionHandlers.get(opt.getOpt());
			currOptionHandler.handleOption(opt, retVal);
		}
		return retVal;
	}

	private void handleNumAnswerSets(Option opt, AlphaConfig cfg) {

	}

	private void handleHelp(Option opt, AlphaConfig cfg) {
		HelpFormatter formatter = new HelpFormatter();
		// Unfortunately, commons-cli does not offer a method of simply rendering a help
		// message into a string, therefore the ByteArrayOutputStream..
		ByteArrayOutputStream helpBuffer = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(helpBuffer);
		formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, this.cmdSyntax, "", CommandLineParser.CLI_OPTS, HelpFormatter.DEFAULT_LEFT_PAD,
				HelpFormatter.DEFAULT_DESC_PAD, "");
		pw.flush();
		this.abortAction.accept(helpBuffer.toString());
	}

	private void handleInput(Option opt, AlphaConfig cfg) {

	}

	private void handleGrounder(Option opt, AlphaConfig cfg) {
		cfg.setGrounderName(opt.getValue(AlphaConfig.DEFAULT_GROUNDER_NAME));
	}

	private void handleSolver(Option opt, AlphaConfig cfg) {
		cfg.setSolverName(opt.getValue(AlphaConfig.DEFAULT_SOLVER_NAME));
	}

	private void handleNogoodStore(Option opt, AlphaConfig cfg) {
		cfg.setNogoodStoreName(opt.getValue(AlphaConfig.DEFAULT_NOGOOD_STORE_NAME));
	}

	private void handleFilters(Option opt, AlphaConfig cfg) {

	}

	private void handleAspString(Option opt, AlphaConfig cfg) {

	}

	private void handleSort(Option opt, AlphaConfig cfg) {
		cfg.setSortAnswerSets(true);
	}

	private void handleDeterministic(Option opt, AlphaConfig cfg) {
		cfg.setDeterministic(true);
	}

	private void handleSeed(Option opt, AlphaConfig cfg) {
		//int seed = Integer.valueOf(opt.getValue(defaultValue))
	}

	private void handleInternalChecks(Option opt, AlphaConfig cfg) {

	}

	private void handleBranchingHeuristic(Option opt, AlphaConfig cfg) {

	}

	private void handleQuiet(Option opt, AlphaConfig cfg) {

	}

	private void handleLiterate(Option opt, AlphaConfig cfg) {

	}

	private void handleStats(Option opt, AlphaConfig cfg) {

	}

	private void handleNoJustification(Option opt, AlphaConfig cfg) {

	}

	private void handleNormalizationGrid(Option opt, AlphaConfig cfg) {

	}

}
