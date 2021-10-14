package at.ac.tuwien.kr.alpha.solver;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultSolverTest {

	/**
	 * Detailed reproduction test-case for github issue #239.
	 */
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
		config.setReplayChoices(Arrays.asList(//
				20,  //_R_("3","{K->3,N->1,T->0}")
				25,  //_R_("3","{K->3,N->1,T->1}")
				35,  //_R_("3","{K->3,N->2,T->0}")
				55,  //_R_("3","{K->3,N->3,T->1}")
				90,  //_R_("3","{K->3,N->5,T->2}")
				95,	 //_R_("3","{K->3,N->6,T->0}")
				284, //_R_("8","{K->3,N->4,T->0}")
				166, //_R_("4","{K->3,N->2,T->1}")
				100, //_R_("3","{K->3,N->6,T->1}")
				289, //_R_("8","{K->3,N->4,T->1}")
				105, //_R_("3","{K->3,N->6,T->2}")
				451, //_R_("9","{K->3,N->9,T->2}")
				445, //_R_("9","{K->3,N->9,T->0}")
				439, //_R_("9","{K->3,N->8,T->1}")
				448, //_R_("9","{K->3,N->9,T->1}")
				433, //_R_("9","{K->3,N->7,T->2}")
				427, //_R_("9","{K->3,N->7,T->0}")
				442, //_R_("9","{K->3,N->8,T->2}")
				421, //_R_("9","{K->3,N->6,T->1}")
				415, //_R_("9","{K->3,N->5,T->2}")
				436, //_R_("9","{K->3,N->8,T->0}")
				409, //_R_("9","{K->3,N->5,T->0}")
				430, //_R_("9","{K->3,N->7,T->1}")
				397, //_R_("9","{K->3,N->3,T->2}")
				391, //_R_("9","{K->3,N->3,T->0}")
				424, //_R_("9","{K->3,N->6,T->2}")
				385, //_R_("9","{K->3,N->2,T->1}")
				379, //_R_("9","{K->3,N->1,T->2}")
				418, //_R_("9","{K->3,N->6,T->0}")
				373, //_R_("9","{K->3,N->1,T->0}")
				412, //_R_("9","{K->3,N->5,T->1}")
				406, //_R_("9","{K->3,N->4,T->2}")
				394, //_R_("9","{K->3,N->3,T->1}")
				388, //_R_("9","{K->3,N->2,T->2}")
				382, //_R_("9","{K->3,N->2,T->0}")
				244, //_R_("8","{K->3,N->1,T->1}")
				232, //_R_("4","{K->3,N->9,T->2}")
				208  //_R_("4","{K->3,N->7,T->0}")
		));
		Alpha alpha = new Alpha(config);
		Optional<AnswerSet> answerSet = alpha.solve(parsedProgram).findFirst();
		assertTrue(answerSet.isPresent());
	}

}
