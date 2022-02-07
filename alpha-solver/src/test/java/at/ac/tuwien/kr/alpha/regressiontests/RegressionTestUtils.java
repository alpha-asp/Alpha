package at.ac.tuwien.kr.alpha.regressiontests;

import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertAnswerSetsEqual;
import static at.ac.tuwien.kr.alpha.test.AlphaAssertions.assertAnswerSetsEqualWithBase;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.function.Executable;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.Solver;
import at.ac.tuwien.kr.alpha.api.config.Heuristic;
import at.ac.tuwien.kr.alpha.api.config.InputConfig;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.impl.AlphaFactory;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;

public final class RegressionTestUtils {

	private RegressionTestUtils() {
		throw new AssertionError("Cannot instantiate utility class!");
	}
	
	public static void runWithTimeout(SystemConfig cfg, long baseTimeout, long timeoutFactor, Executable action) {
		long timeout = cfg.isDebugInternalChecks() ? timeoutFactor * baseTimeout : baseTimeout;
		assertTimeoutPreemptively(Duration.ofMillis(timeout), action);
	}

	public static Solver buildSolverForRegressionTest(String programString, SystemConfig cfg) {
		Alpha alpha = AlphaFactory.newAlpha(cfg);
		InputProgram program = alpha.readProgramString(programString);
		return alpha.prepareSolverFor(program, InputConfig.DEFAULT_FILTER);
	}

	public static Solver buildSolverForRegressionTest(InputProgram program, SystemConfig cfg) {
		Alpha alpha = AlphaFactory.newAlpha(cfg);
		return alpha.prepareSolverFor(program, InputConfig.DEFAULT_FILTER);
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(String program, SystemConfig cfg) {
		Alpha alpha = AlphaFactory.newAlpha(cfg);
		InputProgram in = alpha.readProgramString(program);
		return alpha.solve(in).collect(Collectors.toSet());
	}

	public static Set<AnswerSet> collectRegressionTestAnswerSets(InputProgram program, SystemConfig cfg) {
		return AlphaFactory.newAlpha(cfg)
			.solve(program)
			.collect(Collectors.toSet());
	}

	private static Set<AnswerSet> solveForConfig(String programString, SystemConfig cfg) {
		Alpha alpha = AlphaFactory.newAlpha(cfg);
		InputProgram program = alpha.readProgramString(programString);
		return alpha.solve(program).collect(Collectors.toSet());
	}

	public static void assertRegressionTestAnswerSets(SystemConfig cfg, String programString, String... expectedAnswerSets) {
		assertAnswerSetsEqual(expectedAnswerSets, solveForConfig(programString, cfg));		
	}

	public static void assertRegressionTestAnswerSetsWithBase(SystemConfig cfg, String programString, String base, String... expectedAnswerSets) {
		assertAnswerSetsEqualWithBase(base, expectedAnswerSets, solveForConfig(programString, cfg));		
	}

	public static void ignoreTestForNaiveSolver(SystemConfig cfg) {
		Assumptions.assumeFalse(cfg.getSolverName().equals("naive"));
	}

	public static void ignoreTestForSimplifiedSumAggregates(SystemConfig cfg) {
		Assumptions.assumeTrue(cfg.getAggregateRewritingConfig().isSupportNegativeValuesInSums());
	}

	public static void ignoreTestForNonDefaultDomainIndependentHeuristics(SystemConfig cfg) {
		Assumptions.assumeTrue(cfg.getBranchingHeuristic() == Heuristic.VSIDS);
	}

}
