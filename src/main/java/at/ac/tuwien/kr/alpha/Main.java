package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedTreeVisitor;
import at.ac.tuwien.kr.alpha.grounder.transformation.IdentityProgramTransformation;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

	private static final String DEFAULT_GROUNDER = "naive";
	private static final String DEFAULT_SOLVER = "default";

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
		inputOption.setRequired(true);
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

		Option filterOption = new Option("f", OPT_FILTER, true, "predicates to show when printing answer sets");
		filterOption.setArgs(1);
		filterOption.setArgName("filter");
		filterOption.setValueSeparator(',');
		options.addOption(filterOption);

		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar alpha.jar\njava -jar alpha_bundled.jar", options);
			System.exit(1);
			return;
		}

		if (commandLine.hasOption(OPT_HELP)) {
			HelpFormatter formatter = new HelpFormatter();
			// TODO(flowlo): This is quite optimistic. How do we know that the program
			// really was invoked as "java -jar ..."?
			formatter.printHelp("java -jar alpha.jar OR java -jar alpha_bundled.jar", options);
			System.exit(0);
			return;
		}

		java.util.function.Predicate<Predicate> filter = p -> true;

		if (commandLine.hasOption(OPT_FILTER)) {
			Set<String> desiredPredicates = new HashSet<>(Arrays.asList(commandLine.getOptionValues(OPT_FILTER)));
			filter = p -> {
				return desiredPredicates.contains(p.getPredicateName());
			};
		}

		int limit = 0;

		try {
			Number n = (Number)commandLine.getParsedOptionValue(OPT_NUM_AS);
			if (n != null) {
				limit = n.intValue();
			}
		} catch (ParseException e) {
			bailOut("Failed to parse number of answer sets requested.", e);
		}

		ParsedProgram program = null;
		try {
			program = parseVisit(new FileInputStream(commandLine.getOptionValue(OPT_INPUT)));
		} catch (RecognitionException e) {
			bailOut("Error while parsing input ASP program, see errors above.", e);
		} catch (FileNotFoundException e) {
			bailOut(e.getMessage());
		} catch (IOException e) {
			bailOut("Failed to parse program.", e);
		}

		// Apply program transformations/rewritings (currently none).
		IdentityProgramTransformation programTransformation = new IdentityProgramTransformation();
		ParsedProgram transformedProgram = programTransformation.transform(program);
		Grounder grounder = GrounderFactory.getInstance(
			commandLine.getOptionValue(OPT_GROUNDER, DEFAULT_GROUNDER), transformedProgram, filter
		);

		Solver solver = SolverFactory.getInstance(
			commandLine.getOptionValue(OPT_SOLVER, DEFAULT_SOLVER), grounder
		);

		Stream<AnswerSet> stream = solver.stream();

		if (limit > 0) {
			stream = stream.limit(limit);
		}

		stream.forEach(System.out::println);
	}

	private static void bailOut(String format, Object... arguments) {
		LOGGER.error(format, arguments);
		System.exit(1);
	}

	public static ParsedProgram parseVisit(InputStream is) throws IOException {
		/*
		// In order to require less memory: use unbuffered streams and avoid constructing a full parse tree.
		ASPCore2Lexer lexer = new ASPCore2Lexer(new UnbufferedCharStream(is));
		lexer.setTokenFactory(new CommonTokenFactory(true));
		final ASPCore2Parser parser = new ASPCore2Parser(new UnbufferedTokenStream<>(lexer));
		parser.setBuildParseTree(false);
		*/
		CommonTokenStream tokens = new CommonTokenStream(
			new ASPCore2Lexer(
				new ANTLRInputStream(is)
			)
		);
		final ASPCore2Parser parser = new ASPCore2Parser(tokens);

		// Try SLL parsing mode (faster but may terminate incorrectly).
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());

		final SwallowingErrorListener errorListener = new SwallowingErrorListener();

		ASPCore2Parser.ProgramContext programContext;
		try {
			// Parse program
			programContext = parser.program();
		} catch (ParseCancellationException e) {
			// Recognition exception may be caused simply by SLL parsing failing,
			// retry with LL parser and DefaultErrorStrategy printing errors to console.
			if (e.getCause() instanceof RecognitionException) {
				tokens.reset();
				parser.addErrorListener(errorListener);
				parser.addErrorListener(ConsoleErrorListener.INSTANCE);
				parser.setErrorHandler(new DefaultErrorStrategy());
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				// Re-run parse.
				programContext = parser.program();
			} else {
				throw e;
			}
		}

		// If the our SwallowingErrorListener has handled some exception during parsing
		// just re-throw that exception.
		// At this time, error messages will be already printed out to standard error
		// because ANTLR by default adds an org.antlr.v4.runtime.ConsoleErrorListener
		// to every parser.
		// That ConsoleErrorListener will print useful messages, but not report back to
		// our code.
		// org.antlr.v4.runtime.BailErrorStrategy cannot be used here, because it would
		// abruptly stop parsing as soon as the first error is reached (i.e. no recovery
		// is attempted) and the user will only see the first error encountered.
		if (errorListener.getRecognitionException() != null) {
			throw errorListener.getRecognitionException();
		}

		// Construct internal program representation.
		ParsedTreeVisitor visitor = new ParsedTreeVisitor();
		return (ParsedProgram) visitor.visitProgram(programContext);
	}
}
