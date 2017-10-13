package at.ac.tuwien.kr.alpha.grounder.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.io.IOException;

public interface Parser<T> {
	T parse(CharStream stream) throws IOException;

	default T parse(String s) throws IOException {
		return parse(CharStreams.fromString(s));
	}
}
