package at.ac.tuwien.kr.alpha.api.program;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;

public interface ProgramParser {

	default ASPCore2Program parse(String programString) {
		return parse(programString, Collections.emptyMap());
	}

	default ASPCore2Program parse(InputStream programSource) {
		return parse(programSource, Collections.emptyMap());
	}

	default ASPCore2Program parse(Path programPath) {
		return parse(programPath, Collections.emptyMap());
	}

	default ASPCore2Program parse(Path... programSources) {
		return parse(Collections.emptyMap(), programSources);
	}

	default ASPCore2Program parse(Iterable<Path> programSources) {
		return parse(programSources, Collections.emptyMap());
	}

	ASPCore2Program parse(String programString, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	ASPCore2Program parse(InputStream programSource, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	ASPCore2Program parse(Path programPath, Map<String, PredicateInterpretation> externalPredicateDefinitions);

	ASPCore2Program parse(Map<String, PredicateInterpretation> externalPredicateDefinitions, Path... programSources);

	ASPCore2Program parse(Iterable<Path> programSources, Map<String, PredicateInterpretation> externalPredicateDefinitions);

}
