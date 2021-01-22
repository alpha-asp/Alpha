package at.ac.tuwien.kr.alpha.core.programs;

import org.antlr.v4.runtime.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;

public class Programs {

	private Programs() {
		throw new AssertionError("This is a pure utility class and should therefore not be instantiated!");
	}

	public static InputProgramImpl fromInputStream(InputStream is, Map<String, PredicateInterpretation> externals) throws IOException {
		ProgramParserImpl parser = new ProgramParserImpl(externals);
		return parser.parse(CharStreams.fromStream(is));
	}

}
