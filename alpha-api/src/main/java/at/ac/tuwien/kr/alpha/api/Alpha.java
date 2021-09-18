package at.ac.tuwien.kr.alpha.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;

public interface Alpha {

	InputProgram readProgram(InputConfig cfg) throws IOException;

	InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException;

	InputProgram readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException;

	InputProgram readProgramString(String aspString, Map<String, PredicateInterpretation> externals);

	InputProgram readProgramString(String aspString);

	DebugSolvingContext prepareDebugSolve(final InputProgram program);

	DebugSolvingContext prepareDebugSolve(final NormalProgram program);	
	
	DebugSolvingContext prepareDebugSolve(final InputProgram program, java.util.function.Predicate<Predicate> filter);

	DebugSolvingContext prepareDebugSolve(final NormalProgram program, java.util.function.Predicate<Predicate> filter);
	
	Stream<AnswerSet> solve(InputProgram program);

	Stream<AnswerSet> solve(InputProgram program, java.util.function.Predicate<Predicate> filter);

	Stream<AnswerSet> solve(NormalProgram program);

	Stream<AnswerSet> solve(NormalProgram program, java.util.function.Predicate<Predicate> filter);
	
	NormalProgram normalizeProgram(InputProgram program);
	
	SystemConfig getConfig();
	
	Solver prepareSolverFor(InputProgram program, java.util.function.Predicate<Predicate> filter);

	Solver prepareSolverFor(NormalProgram program, java.util.function.Predicate<Predicate> filter);
	
}
