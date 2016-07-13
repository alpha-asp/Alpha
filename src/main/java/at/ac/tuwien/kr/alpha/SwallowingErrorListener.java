package at.ac.tuwien.kr.alpha;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class SwallowingErrorListener extends BaseErrorListener {
	RecognitionException recognitionException;

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
		this.recognitionException = e;
	}

	public RecognitionException getRecognitionException() {
		return recognitionException;
	}
}
