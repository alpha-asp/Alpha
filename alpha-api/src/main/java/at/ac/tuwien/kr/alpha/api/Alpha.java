package at.ac.tuwien.kr.alpha.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.program.InputProgram;
import at.ac.tuwien.kr.alpha.api.program.Predicate;

public interface Alpha {

	InputProgram readProgram(InputConfig cfg) throws IOException;

	InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException;

	InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException;

	InputProgram readProgramString(String aspString, Map<String, PredicateInterpretation> externals);

	InputProgram readProgramString(String aspString);

	Stream<AnswerSet> solve(InputProgram program);

	Stream<AnswerSet> solve(InputProgram program, java.util.function.Predicate<Predicate> filter);

}
