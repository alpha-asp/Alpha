package at.ac.tuwien.kr.alpha.api.program;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;

public interface ProgramParser {

	default InputProgram parse(String programString) {
		return parse(programString, Collections.emptyMap());
	}

	default InputProgram parse(InputStream programSource) {
		return parse(programSource, Collections.emptyMap());
	}

	default InputProgram parse(Path programPath) {
		return parse(programPath, Collections.emptyMap());
	}

	default InputProgram parse(Path... programSources) {
		return parse(Collections.emptyMap(), programSources);
	}

	default InputProgram parse(Iterable<Path> programSources) {
		return parse(programSources, Collections.emptyMap());
	}

	InputProgram parse(String programString, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	InputProgram parse(InputStream programSource, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	InputProgram parse(Path programPath, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	InputProgram parse(Map<String, PredicateInterpretation> externalPredicateDefinitions, Path... programSources);

	InputProgram parse(Iterable<Path> programSources, Map<String, PredicateInterpretation> externalPredicateDefinitions);

}
