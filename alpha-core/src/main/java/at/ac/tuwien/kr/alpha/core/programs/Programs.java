package at.ac.tuwien.kr.alpha.core.programs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.core.parser.AbstractProgramParser;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ProgramParser;

public class Programs {

	private Programs() {
		throw new AssertionError("This is a pure utility class and should therefore not be instantiated!");
	}

	public static InputProgram fromInputStream(InputStream is, Map<String, PredicateInterpretation> externals) throws IOException {
		AbstractProgramParser parser = new ASPCore2ProgramParser();
		return parser.parse(CharStreams.fromStream(is), externals);
	}

}
