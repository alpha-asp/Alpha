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
	private static final String OPT_NUM_AS  = "numAS";

	private static CommandLine commandLine;

	public static void main(String[] args) {
		final Options options = new Options();

		Option numAnswerSetsOption = new Option("n", OPT_NUM_AS, true, "the number of Answer Sets to compute");
		numAnswerSetsOption.setArgName("number");
		numAnswerSetsOption.setRequired(true);
		numAnswerSetsOption.setArgs(1);
		options.addOption(numAnswerSetsOption);


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

		String numAnswerSetsCLI = commandLine.getOptionValue(OPT_NUM_AS, "-1");
		int numAnswerSetsRequested = Integer.parseInt(numAnswerSetsCLI);


		ParsedProgram program = null;
		try {
			program = parseVisit(new FileInputStream(commandLine.getOptionValue(OPT_INPUT)));
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
		DummyGrounder grounder = new DummyGrounder();
		grounder.initialize(transformedProgram);

		// initialize the solver
		DummySolver solver = new DummySolver(grounder);

		// start solver
		solver.computeAnswerSets(numAnswerSetsRequested);
	}

	private static boolean parsingError;

	static ParsedProgram parseVisit(InputStream is) throws IOException {
		// prepare parser
		ASPCore2Lexer lexer = new ASPCore2Lexer(new ANTLRInputStream(is));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ASPCore2Parser parser = new ASPCore2Parser(tokens);

		// record eventual parsing errors
		parsingError = false;
		parser.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
				parsingError = true;
			}
		});

		// parse program
		ASPCore2Parser.ProgramContext programContext = parser.program();

		if (parsingError) {
			System.err.println("Error while parsing input ASP program using modules, see errors above.");
			System.exit(-1);
		}

		// construct internal program representation
		ParsedTreeVisitor visitor = new ParsedTreeVisitor();
		return (ParsedProgram) visitor.visitProgram(programContext);
	}
}
