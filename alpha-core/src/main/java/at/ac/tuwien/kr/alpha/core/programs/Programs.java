package at.ac.tuwien.kr.alpha.core.programs;

import org.antlr.v4.runtime.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParser;

public class Programs {

	private Programs() {
		throw new AssertionError("This is a pure utility class and should therefore not be instantiated!");
	}

	public static InputProgram fromInputStream(InputStream is, Map<String, PredicateInterpretation> externals) throws IOException {
		ProgramParser parser = new ProgramParser(externals);
		return parser.parse(CharStreams.fromStream(is));
	}

}
