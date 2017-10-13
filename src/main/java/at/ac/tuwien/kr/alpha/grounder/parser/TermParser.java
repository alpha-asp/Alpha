package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.CustomErrorListener;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2TermParser;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;

public class TermParser implements Parser<Term> {
	@Override
	public Term parse(CharStream stream) throws IOException {
		CommonTokenStream tokens = new CommonTokenStream(
			new ASPCore2Lexer(stream)
		);
		final ASPCore2TermParser parser = new ASPCore2TermParser(tokens);

		// Try SLL parsing mode (faster but may terminate incorrectly).
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());

		final CustomErrorListener errorListener = new CustomErrorListener("");

		ASPCore2TermParser.TermContext termContext;
		try {
			// Parse program
			termContext = parser.term();
		} catch (ParseCancellationException e) {
			// Recognition exception may be caused simply by SLL parsing failing,
			// retry with LL parser and DefaultErrorStrategy printing errors to console.
			throw e;
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
		TermParseTreeVisitor visitor = new TermParseTreeVisitor();
		return (Term) visitor.visit(termContext);
	}
}
