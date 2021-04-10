package at.ac.tuwien.kr.alpha.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;

public interface Alpha {

	ASPCore2Program readProgram(InputConfig cfg) throws IOException;

	ASPCore2Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, List<String> paths) throws IOException;

	ASPCore2Program readProgramFiles(boolean literate, Map<String, PredicateInterpretation> externals, Path... paths) throws IOException;

	ASPCore2Program readProgramString(String aspString, Map<String, PredicateInterpretation> externals);

	ASPCore2Program readProgramString(String aspString);

	DebugSolvingResult debugSolve(ASPCore2Program program);
	
	DebugSolvingResult debugSolve(NormalProgram program);
	
	Stream<AnswerSet> solve(ASPCore2Program program);

	Stream<AnswerSet> solve(ASPCore2Program program, java.util.function.Predicate<Predicate> filter);

	Stream<AnswerSet> solve(NormalProgram program);

	Stream<AnswerSet> solve(NormalProgram program, java.util.function.Predicate<Predicate> filter);
	
	NormalProgram normalizeProgram(ASPCore2Program program);
	
	SystemConfig getConfig();
	
	//CompiledProgram performProgramPreprocessing(CompiledProgram prog);
	
	Solver prepareSolverFor(ASPCore2Program program, java.util.function.Predicate<Predicate> filter);

	Solver prepareSolverFor(NormalProgram program, java.util.function.Predicate<Predicate> filter);
	
}
