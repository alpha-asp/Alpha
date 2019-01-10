package at.ac.tuwien.kr.alpha.config;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.Predicate;

public class CommandLineParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineParser.class);

	// "special", i.e. non-configuration options
	private static final Option OPT_HELP = Option.builder("h").longOpt("help").hasArg(false).desc("shows this help").build();

	// input-specific options
	private static final Option OPT_INPUT = Option.builder("i").longOpt("input").hasArg(true).argName("file").type(FileInputStream.class)
			.desc("read ASP program from this file").build();
	private static final Option OPT_NUM_ANSWER_SETS = Option.builder("n").longOpt("numAS").hasArg(true).argName("number").type(Integer.class)
			.desc("the number of answer sets to compute (default: compute all)").build();
	private static final Option OPT_FILTER = Option.builder("f").longOpt("filter").hasArg(true).argName("filter").valueSeparator(',')
			.desc("predicates to show when printing answer sets").build();
	private static final Option OPT_ASPSTRING = Option.builder("str").longOpt("aspstring").hasArg(true).argName("program").type(String.class)
			.desc("provide the asp program as a string").build();
	private static final Option OPT_LITERATE = Option.builder("l").longOpt("literate")
			.desc("enable literate programming mode (default: " + InputConfig.DEFAULT_LITERATE + ")").build();

	// general system-wide config
	private static final Option OPT_GROUNDER = Option.builder("g").longOpt("grounder").hasArg(true).argName("grounder")
			.desc("the grounder implementation to use (default: " + AlphaConfig.DEFAULT_GROUNDER_NAME + ")").build();
	private static final Option OPT_SOLVER = Option.builder("s").longOpt("solver").hasArg(true).argName("solver")
			.desc("the solver implementation to use (default: " + AlphaConfig.DEFAULT_SOLVER_NAME + ")").build();
	private static final Option OPT_NOGOOD_STORE = Option.builder("r").longOpt("store").hasArg(true).argName("store")
			.desc("the nogood store to use (default: " + AlphaConfig.DEFAULT_NOGOOD_STORE_NAME + ")").build();
	private static final Option OPT_SORT = Option.builder("sort").longOpt("sort").hasArg(false)
			.desc("sort answer sets (default: " + AlphaConfig.DEFAULT_SORT_ANSWER_SETS + ")").build();
	private static final Option OPT_DETERMINISTIC = Option.builder("d").longOpt("deterministic").hasArg(false)
			.desc("disables randomness (default: " + AlphaConfig.DEFAULT_DETERMINISTIC + ")").build();
	private static final Option OPT_SEED = Option.builder("e").longOpt("seed").hasArg(true).argName("seed").type(Integer.class)
			.desc("set seed (default: System.nanoTime())").build();
	private static final Option OPT_DEBUG_INTERNAL_CHECKS = Option.builder("dbg").longOpt("DebugEnableInternalChecks")
			.desc("run additional (time-consuming) safety checks (default: " + AlphaConfig.DEFAULT_DEBUG_INTERNAL_CHECKS + ")").build();
	private static final Option OPT_BRANCHING_HEURISTIC = Option.builder("b").longOpt("branchingHeuristic").hasArg(true).argName("heuristic")
			.desc("the branching heuristic to use (default: " + AlphaConfig.DEFAULT_BRANCHING_HEURISTIC_NAME + ")").build();
	private static final Option OPT_QUIET = Option.builder("q").longOpt("quiet").desc("do not print answer sets (default: " + AlphaConfig.DEFAULT_QUIET)
			.build();
	private static final Option OPT_STATS = Option.builder("st").longOpt("stats").desc("print statistics (default: " + AlphaConfig.DEFAULT_PRINT_STATS + ")")
			.build();
	private static final Option OPT_NO_JUSTIFICATION = Option.builder("dj").longOpt("disableJustifications")
			.desc("disable the search for justifications on must-be-true assigned atoms in the solver (default: "
					+ AlphaConfig.DEFAULT_DISABLE_JUSTIFICATION_SEARCH + ")")
			.build();
	private static final Option OPT_NORMALIZATION_GRID = Option.builder("ng").longOpt("normalizationCountingGrid")
			.desc("use counting grid normalization instead of sorting circuit for #count (default: " + AlphaConfig.DEFAULT_USE_NORMALIZATION_GRID + ")")
			.build();

	private static final Options CLI_OPTS = new Options();

	static {
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_HELP);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NUM_ANSWER_SETS);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_FILTER);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_LITERATE);
		OptionGroup inputSourceMutexGroup = new OptionGroup();
		inputSourceMutexGroup.addOption(CommandLineParser.OPT_INPUT);
		inputSourceMutexGroup.addOption(CommandLineParser.OPT_ASPSTRING);
		// below line is commented out because commons-cli cannot handle arbitrary-depth
		// option groups
		// -> causes problems since calling "java -jar Alpha.jar -h" would cause an
		// error with below line
		// commented in -> check if an input source is specified is done "manually"
		// below
		// inputSourceMutexGroup.setRequired(true);
		CommandLineParser.CLI_OPTS.addOptionGroup(inputSourceMutexGroup);

		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_GROUNDER);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SOLVER);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NOGOOD_STORE);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SORT);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_DETERMINISTIC);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_SEED);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_DEBUG_INTERNAL_CHECKS);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_BRANCHING_HEURISTIC);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_QUIET);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_STATS);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NO_JUSTIFICATION);
		CommandLineParser.CLI_OPTS.addOption(CommandLineParser.OPT_NORMALIZATION_GRID);
	}

	private final Map<String, IOptionHandler<AlphaConfig>> globalOptionHandlers = new HashMap<>();
	private final Map<String, IOptionHandler<InputConfig>> inputOptionHandlers = new HashMap<>();
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

		// help is handled separately, therefore dummy handler
		this.globalOptionHandlers.put(CommandLineParser.OPT_HELP.getOpt(), (o, c) -> {});
		this.globalOptionHandlers.put(CommandLineParser.OPT_GROUNDER.getOpt(), this::handleGrounder);
		this.globalOptionHandlers.put(CommandLineParser.OPT_SOLVER.getOpt(), this::handleSolver);
		this.globalOptionHandlers.put(CommandLineParser.OPT_NOGOOD_STORE.getOpt(), this::handleNogoodStore);
		this.globalOptionHandlers.put(CommandLineParser.OPT_SORT.getOpt(), this::handleSort);
		this.globalOptionHandlers.put(CommandLineParser.OPT_DETERMINISTIC.getOpt(), this::handleDeterministic);
		this.globalOptionHandlers.put(CommandLineParser.OPT_SEED.getOpt(), this::handleSeed);
		this.globalOptionHandlers.put(CommandLineParser.OPT_DEBUG_INTERNAL_CHECKS.getOpt(), this::handleInternalChecks);
		this.globalOptionHandlers.put(CommandLineParser.OPT_BRANCHING_HEURISTIC.getOpt(), this::handleBranchingHeuristic);
		this.globalOptionHandlers.put(CommandLineParser.OPT_QUIET.getOpt(), this::handleQuiet);
		this.globalOptionHandlers.put(CommandLineParser.OPT_STATS.getOpt(), this::handleStats);
		this.globalOptionHandlers.put(CommandLineParser.OPT_NO_JUSTIFICATION.getOpt(), this::handleNoJustification);
		this.globalOptionHandlers.put(CommandLineParser.OPT_NORMALIZATION_GRID.getOpt(), this::handleNormalizationGrid);

		this.inputOptionHandlers.put(CommandLineParser.OPT_NUM_ANSWER_SETS.getOpt(), this::handleNumAnswerSets);
		this.inputOptionHandlers.put(CommandLineParser.OPT_INPUT.getOpt(), this::handleInput);
		this.inputOptionHandlers.put(CommandLineParser.OPT_FILTER.getOpt(), this::handleFilters);
		this.inputOptionHandlers.put(CommandLineParser.OPT_ASPSTRING.getOpt(), this::handleAspString);
		this.inputOptionHandlers.put(CommandLineParser.OPT_LITERATE.getOpt(), this::handleLiterate);
	}

	public AlphaContext parseCommandLine(String[] args) throws ParseException {
		CommandLine commandLine = new DefaultParser().parse(CommandLineParser.CLI_OPTS, args);
		AlphaContext retVal = new AlphaContext();
		AlphaConfig sysConf = new AlphaConfig();
		InputConfig inputConf = new InputConfig();
		IOptionHandler<AlphaConfig> globalOptionHandler;
		IOptionHandler<InputConfig> inputOptionHandler;
		if (commandLine.hasOption(CommandLineParser.OPT_HELP.getOpt())) {
			LOGGER.debug("Found help option!");
			this.handleHelp();
		} else {
			this.validate(commandLine);
		}
		for (Option opt : commandLine.getOptions()) {
			globalOptionHandler = this.globalOptionHandlers.get(opt.getOpt());
			if (globalOptionHandler != null) {
				globalOptionHandler.handleOption(opt, sysConf);
			} else {
				inputOptionHandler = this.inputOptionHandlers.get(opt.getOpt());
				if (inputOptionHandler != null) {
					inputOptionHandler.handleOption(opt, inputConf);
				} else {
					throw new ParseException("Cannot handle option: " + opt.getOpt());
				}
			}
		}
		retVal.setAlphaConfig(sysConf);
		retVal.setInputConfig(inputConf);
		return retVal;
	}

	private void validate(CommandLine commandLine) throws ParseException {
		if (!commandLine.hasOption(CommandLineParser.OPT_INPUT.getOpt()) && !commandLine.hasOption(CommandLineParser.OPT_ASPSTRING.getOpt())) {
			throw new ParseException("Missing input source - need to specifiy either a file (" + CommandLineParser.OPT_INPUT.getOpt() + ") or a string ("
					+ CommandLineParser.OPT_ASPSTRING.getOpt() + ")!");
		}
	}

	private void handleNumAnswerSets(Option opt, InputConfig cfg) {
		String optVal = opt.getValue();
		int limit;
		if (optVal != null) {
			limit = Integer.valueOf(optVal);
			cfg.setNumAnswerSets(limit);
		} else {
			cfg.setNumAnswerSets(InputConfig.DEFAULT_NUM_ANSWER_SETS);
		}
	}

	private void handleHelp() {
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

	private void handleInput(Option opt, InputConfig cfg) {
		String optVal = opt.getValue().trim();
		cfg.getFiles().add(optVal);
		cfg.setSource(InputConfig.InputSource.FILE);
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

	private void handleFilters(Option opt, InputConfig cfg) {
		Set<String> desiredPredicates = new HashSet<>(Arrays.asList(opt.getValues()));
		java.util.function.Predicate<Predicate> filter = p -> desiredPredicates.contains(p.getName());
		cfg.setFilter(filter);
	}

	private void handleAspString(Option opt, InputConfig cfg) {
		cfg.setAspString(opt.getValue());
		cfg.setSource(InputConfig.InputSource.STRING);
	}

	private void handleSort(Option opt, AlphaConfig cfg) {
		cfg.setSortAnswerSets(true);
	}

	private void handleDeterministic(Option opt, AlphaConfig cfg) {
		cfg.setDeterministic(true);
		cfg.setSeed(0);
	}

	private void handleSeed(Option opt, AlphaConfig cfg) {
		cfg.setDeterministic(false);
		String optVal = opt.getValue();
		long seed;
		if (optVal != null) {
			seed = Long.valueOf(optVal);
			cfg.setSeed(seed);
		} else {
			cfg.setSeed(AlphaConfig.DEFAULT_SEED);
		}
	}

	private void handleInternalChecks(Option opt, AlphaConfig cfg) {
		cfg.setDebugInternalChecks(true);
	}

	private void handleBranchingHeuristic(Option opt, AlphaConfig cfg) {
		cfg.setBranchingHeuristicName(opt.getValue(AlphaConfig.DEFAULT_BRANCHING_HEURISTIC_NAME));
	}

	private void handleQuiet(Option opt, AlphaConfig cfg) {
		cfg.setQuiet(true);
	}

	private void handleLiterate(Option opt, InputConfig cfg) {
		cfg.setLiterate(true);
	}

	private void handleStats(Option opt, AlphaConfig cfg) {
		cfg.setPrintStats(true);
	}

	private void handleNoJustification(Option opt, AlphaConfig cfg) {
		cfg.setDisableJustificationSearch(true);
	}

	private void handleNormalizationGrid(Option opt, AlphaConfig cfg) {
		cfg.setUseNormalizationGrid(true);
	}

}
