package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.antlr.AnswerSetsLexer;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.parser.Parser;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.IOException;
import java.util.Set;

public class AnswerSetsParser implements Parser<Set<AnswerSet>> {
	@Override
	public Set<AnswerSet> parse(CharStream stream) throws IOException {
		CommonTokenStream tokens = new CommonTokenStream(
			new AnswerSetsLexer(stream)
		);
		final at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser parser = new at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser(tokens);

		// Try SLL parsing mode (faster but may terminate incorrectly).
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());

		final CustomErrorListener errorListener = new CustomErrorListener("");

		at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.AnswerSetsContext answerSetsContext;
		try {
			// Parse program
			answerSetsContext = parser.answerSets();
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
		AnswerSetsParseTreeVisitor visitor = new AnswerSetsParseTreeVisitor();
		return (Set<AnswerSet>) visitor.visit(answerSetsContext);
	}
}
