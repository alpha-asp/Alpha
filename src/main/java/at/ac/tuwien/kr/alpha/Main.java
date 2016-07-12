package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedTreeVisitor;
import at.ac.tuwien.kr.alpha.grounder.transformation.IdentityProgramTransformation;
import at.ac.tuwien.kr.alpha.solver.DummySolver;
import org.antlr.v4.runtime.*;
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

	private static CommandLine commandLine;

	public static void main(String[] args) {
		final Options options = new Options();

		Option inputOption = new Option("i", OPT_INPUT, true, "read the ASP program from this file");
		inputOption.setArgName("file");
		inputOption.setRequired(true);
		inputOption.setArgs(1);
		options.addOption(inputOption);

		Option helpOption = new Option("h", OPT_HELP, false, "show this help");
		options.addOption(helpOption);

		try {
			commandLine = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(1);
			return;
		}

		if (commandLine.hasOption(OPT_HELP)) {
			HelpFormatter formatter = new HelpFormatter();
			// TODO(flowlo): This is quite optimistic. How do we know that the program
			// really was invoked as "java -jar ..."?
			formatter.printHelp("java -jar alpha.jar", options);
			System.exit(0);
			return;
		}

		ParsedProgram program = null;
		try {
			program = parseVisit(new FileInputStream(commandLine.getOptionValue(OPT_INPUT)));
		} catch (RecognitionException e) {
			System.err.println("Error while parsing input ASP program, see errors above.");
			System.exit(1);
		} catch (FileNotFoundException e) {
			LOG.fatal(e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			LOG.fatal(e);
			System.exit(1);
		}

		// apply program transformations/rewritings (currently none)
		IdentityProgramTransformation programTransformation = new IdentityProgramTransformation();
		ParsedProgram transformedProgram = programTransformation.transform(program);

		// initialize the grounder
		DummyGrounder grounder = new DummyGrounder(transformedProgram);

		// initialize the solver
		DummySolver solver = new DummySolver(grounder);
		// TODO: Start solver
	}

	static ParsedProgram parseVisit(InputStream is) throws IOException {
		final ASPCore2Parser parser = new ASPCore2Parser(
			new CommonTokenStream(
				new ASPCore2Lexer(
					new ANTLRInputStream(is)
				)
			)
		);

		final SwallowingErrorListener errorListener = new SwallowingErrorListener();
		parser.addErrorListener(errorListener);

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
