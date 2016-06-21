package at.ac.tuwien.kr.alpha;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

		ASPCore2Lexer lexer;
		try {
			lexer = new ASPCore2Lexer(new ANTLRInputStream(new FileInputStream(commandLine.getOptionValue(OPT_INPUT))));
		} catch (FileNotFoundException fnfe) {
			LOG.fatal(fnfe.getMessage());
			System.exit(1);
			return;
		} catch (IOException ioe) {
			LOG.fatal(ioe);
			System.exit(1);
			return;
		}

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ASPCore2Parser parser = new ASPCore2Parser(tokens);

		ASPCore2Parser.ProgramContext programContext = parser.program();

		ParseTreeWalker walker = new ParseTreeWalker();
		Listener listener = new Listener();
		walker.walk(listener, programContext);

		// TODO: Do something with what we just parsed?!
	}
}
