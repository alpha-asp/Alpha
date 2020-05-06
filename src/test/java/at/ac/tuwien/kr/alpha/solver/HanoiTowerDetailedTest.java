package at.ac.tuwien.kr.alpha.solver;

import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * A more fine-grained test, mostly intended for debugging purposes, of the "simple" instance of the Hanoi Tower
 * problem.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class HanoiTowerDetailedTest {

	private static final String HANOI_TOWER_SRC = "/HanoiTower_Alpha.asp";
	private static final String SIMPLE_INSTANCE = "/HanoiTower_instances/simple.asp";

	@Test
	public void testHanoiTower() throws IOException {
		Alpha alpha = new Alpha();
		// alpha.getConfig().setEvaluateStratifiedPart(false);
		InputProgram.Builder programBuilder = InputProgram.builder();
		ProgramParser parser = new ProgramParser();
		InputStream baseProgStream = HanoiTowerDetailedTest.class.getResourceAsStream(HANOI_TOWER_SRC);
		InputStream instanceStream = HanoiTowerDetailedTest.class.getResourceAsStream(SIMPLE_INSTANCE);
		programBuilder.accumulate(parser.parse(CharStreams.fromStream(baseProgStream)));
		programBuilder.accumulate(parser.parse(CharStreams.fromStream(instanceStream)));
		InputProgram prog = programBuilder.build();
		InternalProgram preprocessed = alpha.performProgramPreprocessing(alpha.normalizeProgram(prog));
		Optional<AnswerSet> solveResult = alpha.solve(preprocessed).findFirst();
		Assert.assertTrue(solveResult.isPresent());
	}

}
