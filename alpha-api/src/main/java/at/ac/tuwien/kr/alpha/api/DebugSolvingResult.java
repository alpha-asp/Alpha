package at.ac.tuwien.kr.alpha.api;

import java.util.stream.Stream;

import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;

public interface DebugSolvingResult {
	
	NormalProgram getNormalizedProgram();
	
	NormalProgram getPreprocessedProgram();
	
	DependencyGraph getDependencyGraph();
	
	ComponentGraph getComponentGraph();
	
	Stream<AnswerSet> answerSets();

}
