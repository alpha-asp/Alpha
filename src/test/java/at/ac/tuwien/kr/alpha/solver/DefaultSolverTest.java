package at.ac.tuwien.kr.alpha.solver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;

public class DefaultSolverTest {
	
	// detailed reproduction test-case for github issue #239
	@Test
	public void testLearnedUnaryNoGoodCausingOutOfOrderLiteralsConflict() throws IOException {
		final ProgramParser parser = new ProgramParser();
		InputProgram.Builder bld = InputProgram.builder();
		bld.accumulate(parser.parse(CharStreams.fromPath(Paths.get("src", "test", "resources", "HanoiTower_Alpha.asp"))));
		bld.accumulate(parser.parse(CharStreams.fromPath(Paths.get("src", "test", "resources", "HanoiTower_instances", "simple.asp"))));
		InputProgram parsedProgram = bld.build();
		
		SystemConfig config = new SystemConfig();
		config.setSolverName("default");
		config.setNogoodStoreName("alpharoaming");
		config.setSeed(0);
		config.setBranchingHeuristic(BranchingHeuristicFactory.Heuristic.valueOf("VSIDS"));
		config.setDebugInternalChecks(true);
		config.setDisableJustificationSearch(false);
		config.setEvaluateStratifiedPart(false);
		config.setReplayChoices(Arrays.asList(21, 26, 36, 56, 91, 96, 285, 166, 101, 290, 106, 451, 445, 439, 448,
			433, 427, 442, 421, 415, 436, 409, 430, 397, 391, 424, 385, 379,
			418, 373, 412, 406, 394, 388, 382, 245, 232, 208
		));
		Alpha alpha = new Alpha(config);
		Optional<AnswerSet> answerSet = alpha.solve(parsedProgram).findFirst();
		assertTrue(answerSet.isPresent());
	}

}
