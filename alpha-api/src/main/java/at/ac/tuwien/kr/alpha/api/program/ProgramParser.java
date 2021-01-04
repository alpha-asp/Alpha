package at.ac.tuwien.kr.alpha.api.program;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;

public interface ProgramParser {

	default Program parse(String programString) {
		return parse(programString, Collections.emptyMap());
	}

	default Program parse(InputStream programSource) {
		return parse(programSource, Collections.emptyMap());
	}

	default Program parse(Path programPath) {
		return parse(programPath, Collections.emptyMap());
	}

	default Program parse(Path... programSources) {
		return parse(Collections.emptyMap(), programSources);
	}

	default Program parse(Iterable<Path> programSources) {
		return parse(programSources, Collections.emptyMap());
	}

	Program parse(String programString, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	Program parse(InputStream programSource, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	Program parse(Path programPath, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	Program parse(Map<String, PredicateInterpretation> externalPredicateDefinitions, Path... programSources);

	Program parse(Iterable<Path> programSources, Map<String, PredicateInterpretation> externalPredicateDefinitions);

}
