package at.ac.tuwien.kr.alpha.api.programs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;

public interface ProgramParser {

	default InputProgram parse(String programString) {
		return parse(programString, Collections.emptyMap());
	}

	default InputProgram parse(InputStream programSource) throws IOException {
		return parse(programSource, Collections.emptyMap());
	}

	default InputProgram parse(Path programPath) throws IOException {
		return parse(programPath, Collections.emptyMap());
	}

	default InputProgram parse(Path... programSources) throws IOException {
		return parse(Collections.emptyMap(), programSources);
	}

	default InputProgram parse(Iterable<Path> programSources) throws IOException {
		return parse(programSources, Collections.emptyMap());
	}

	InputProgram parse(String programString, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	InputProgram parse(InputStream programSource, Map<String, PredicateInterpretation> externalPredicateDefinitions) throws IOException;

	InputProgram parse(Path programPath, Map<String, PredicateInterpretation> externalPredicateDefinitions) throws IOException;

	InputProgram parse(Map<String, PredicateInterpretation> externalPredicateDefinitions, Path... programSources) throws IOException;

	InputProgram parse(Iterable<Path> programSources, Map<String, PredicateInterpretation> externalPredicateDefinitions) throws IOException;

}
