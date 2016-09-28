package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedTreeVisitor;
import at.ac.tuwien.kr.alpha.grounder.transformation.IdentityProgramTransformation;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Main entry point for Alpha.
 */
public class Main {
	private static final Log LOG = LogFactory.getLog(Main.class);

	private static final String OPT_INPUT = "input";
	private static final String OPT_HELP  = "help";
	private static final String OPT_NUM_AS  = "numAS";
	private static final String OPT_GROUNDER = "grounder";
	private static final String OPT_SOLVER = "solver";

	private static final String DEFAULT_GROUNDER = "dummy";
	private static final String DEFAULT_SOLVER = "leutgeb";

	private static CommandLine commandLine;

	public static void main(String[] args) {
		final Options options = new Options();

		Option numAnswerSetsOption = new Option("n", OPT_NUM_AS, true, "the number of Answer Sets to compute");
		numAnswerSetsOption.setArgName("number");
		numAnswerSetsOption.setRequired(true);
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

		int limit = -1;

		try {
			limit = ((Number)commandLine.getParsedOptionValue(OPT_NUM_AS)).intValue();
		} catch (ParseException e) {
			bailOut("Failed to parse number of answer sets requested.");
		}

		if (limit < 1) {
			bailOut("Number of Answer Sets Requested must be a positive integer.");
		}

		ParsedProgram program = null;
		try {
			program = parseVisit(new FileInputStream(commandLine.getOptionValue(OPT_INPUT)));
		} catch (RecognitionException e) {
			bailOut("Error while parsing input ASP program, see errors above.");
		} catch (FileNotFoundException e) {
			bailOut(e.getMessage());
		} catch (IOException e) {
			bailOut(e);
		}

		// Apply program transformations/rewritings (currently none).
		IdentityProgramTransformation programTransformation = new IdentityProgramTransformation();
		ParsedProgram transformedProgram = programTransformation.transform(program);
		Grounder grounder = GrounderFactory.getInstance(
			commandLine.getOptionValue(OPT_GROUNDER, DEFAULT_GROUNDER), transformedProgram
		);

		// TODO(flowlo): Add meaningful filter here, probably by interpreting some flag.
		Solver solver = SolverFactory.getInstance(
			commandLine.getOptionValue(OPT_SOLVER, DEFAULT_SOLVER), grounder, p -> true
		);


		int answerSetCount = 0;
		while (true) {
			AnswerSet as = solver.get();
			if (as == null || answerSetCount == limit) {
				break;
			}
			answerSetCount++;
			System.out.println(as);
		}
		/*Stream.generate(solver)
			.limit(limit)
			.forEach(System.out::println);*/
	}

	private static void bailOut(Object o) {
		LOG.fatal(o);
		System.exit(1);
	}

	public static ParsedProgram parseVisit(InputStream is) throws IOException {
		final ASPCore2Parser parser = new ASPCore2Parser(
			new CommonTokenStream(
				new ASPCore2Lexer(
					new ANTLRInputStream(is)
				)
			)
		);

		final SwallowingErrorListener errorListener = new SwallowingErrorListener();
		parser.addErrorListener(errorListener);

		// Parse program
		ASPCore2Parser.ProgramContext programContext = parser.program();

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
