package at.ac.tuwien.kr.alpha.core.solver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.params.provider.Arguments;

import at.ac.tuwien.kr.alpha.api.config.Heuristic;

public class RegressionTestConfigProvider {

	private static final String DEFAULT_SOLVER_NAME = "default";
	private static final String DEFAULT_GROUNDER_NAME = "naive";
	private static final String DEFAULT_ATOM_STORE = "alpharoaming";
	private static final String DEFAULT_BRANCHING_HEURISTIC = "VSIDS";
	private static final boolean DEFAULT_RESTARTS_ENABLED = false;
	private static final int DEFAULT_RESTART_ITERATIONS = 5;
	private static final String DEFAULT_GROUNDER_TOLERANCE = "strict";
	private static final boolean DEFAULT_DISABLE_INSTANCE_REMOVAL = false;
	private static final boolean DEFAULT_ENABLE_DEBUG_CHECKS = false;

	/**
	 * Creates a list of {@link RegressionTestConfig}s with all config combinations that are to be tested im methods tagged using
	 * "RegressionTest" annotation.
	 * Exact number of combinations depends on the "CI" environment variable that can be used to signal that a test is being run in a CI
	 * environment.
	 *
	 * @return
	 */
	private static List<RegressionTestConfig> buildConfigs() {
		// Check whether we are running in a CI environment.
		boolean ci = Boolean.valueOf(System.getenv("CI"));

		//@formatter:off
		String[] solvers = ci ? new String[]{DEFAULT_SOLVER_NAME, "naive"} : new String[]{DEFAULT_SOLVER_NAME};
		String grounder = DEFAULT_GROUNDER_NAME;
		String[] atomStores = ci ? new String[]{DEFAULT_ATOM_STORE, "naive"} : new String[]{DEFAULT_ATOM_STORE};
		String[] heuristics = ci ? nonDeprecatedHeuristics() : new String[]{"NAIVE", DEFAULT_BRANCHING_HEURISTIC};
		boolean[] restartEnabledValues = new boolean[]{DEFAULT_RESTARTS_ENABLED, true};
		int[] restartIterationsValues = new int[]{DEFAULT_RESTART_ITERATIONS};
		String[] gtcValues = new String[]{DEFAULT_GROUNDER_TOLERANCE, "permissive"};
		String gtrValue = DEFAULT_GROUNDER_TOLERANCE;
		boolean[] disableInstanceRemovalValues = ci ? new boolean[]{DEFAULT_DISABLE_INSTANCE_REMOVAL, true} : new boolean[]{DEFAULT_DISABLE_INSTANCE_REMOVAL};
		boolean[] evaluateStratifiedValues = new boolean[]{false, true};
		boolean[] enableDebugChecksValues = new boolean[]{DEFAULT_ENABLE_DEBUG_CHECKS, true};
		//@formatter:on

		// NOTE:
		// It is handy to set the seed for reproducing bugs. However, the reverse is also sometimes needed:
		// A test case fails, now what was the seed that "caused" it? To allow this, we need full control over
		// the seed, so we generate one in any case.
		// If your test case fails you can inspect the property called "seed" of AbstractSolverTests and extract
		// its value.
		String seedProperty = System.getProperty("seed", ci ? "0" : "");
		long seed = seedProperty.isEmpty() ? (new Random().nextLong()) : Long.valueOf(seedProperty);

		List<RegressionTestConfig> configsToTest = new ArrayList<>();
		for (String solverName : solvers) {
			for (String atomStoreName : atomStores) {
				for (String branchingHeuristicName : heuristics) {
					for (boolean restartsEnabled : restartEnabledValues) {
						for (int restartIterations : restartIterationsValues) {
							for (String grounderTolerance : gtcValues) {
								for (boolean disableInstanceRemoval : disableInstanceRemovalValues) {
									for (boolean evaluateStratified : evaluateStratifiedValues) {
										for (boolean enableDebugChecks : enableDebugChecksValues) {
											configsToTest.add(new RegressionTestConfig(
													solverName, grounder, atomStoreName,
													Heuristic.valueOf(branchingHeuristicName),
													restartsEnabled, restartIterations, seed, enableDebugChecks,
													grounderTolerance, gtrValue, disableInstanceRemoval,
													evaluateStratified, true, true));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return configsToTest;

	}

	/**
	 * Provides {@link RegressionTestConfig}s specifically for tests concerned with AggregateRewriting.
	 * All parameters fixed to default values except stratified evaluation, sorting grid encoding for count rewriting
	 * and negative sum element support.
	 *
	 * @return
	 */
	private static List<RegressionTestConfig> buildConfigsForAggregateTests() {
		List<RegressionTestConfig> configsToTest = new ArrayList<>();

		boolean[] evaluateStratifiedValues = new boolean[]{true, false};
		boolean[] useSortingGridValues = new boolean[]{true, false};
		boolean[] supportNegativeSumElementsValues = new boolean[]{true, false};

		for (boolean evalStratified : evaluateStratifiedValues) {
			for (boolean useSortingGrid : useSortingGridValues) {
				for (boolean supportNegativeElements : supportNegativeSumElementsValues) {
					configsToTest.add(
							new RegressionTestConfig(
									DEFAULT_SOLVER_NAME, DEFAULT_GROUNDER_NAME, DEFAULT_ATOM_STORE, Heuristic.valueOf(DEFAULT_BRANCHING_HEURISTIC),
									DEFAULT_RESTARTS_ENABLED, DEFAULT_RESTART_ITERATIONS, 0, DEFAULT_ENABLE_DEBUG_CHECKS, DEFAULT_GROUNDER_TOLERANCE, DEFAULT_GROUNDER_TOLERANCE, DEFAULT_DISABLE_INSTANCE_REMOVAL,
									evalStratified,
									useSortingGrid, supportNegativeElements));
				}
			}
		}

		return configsToTest;
	}

	public static List<Arguments> provideConfigs() {
		List<Arguments> retVal = new ArrayList<>();
		for (RegressionTestConfig cfg : buildConfigs()) {
			retVal.add(Arguments.of(cfg));
		}
		return retVal;
	}

	public static List<Arguments> provideAggregateTestConfigs() {
		List<Arguments> retVal = new ArrayList<>();
		for (RegressionTestConfig cfg : buildConfigsForAggregateTests()) {
			retVal.add(Arguments.of(cfg));
		}
		return retVal;
	}

	private static final String[] nonDeprecatedHeuristics() {
		final List<String> nonDeprecatedHeuristicsNames = new ArrayList<>();
		for (Field field : Heuristic.class.getFields()) {
			if (field.getAnnotation(Deprecated.class) == null) {
				nonDeprecatedHeuristicsNames.add(field.getName());
			}
		}
		return nonDeprecatedHeuristicsNames.toArray(new String[]{});
	}
}
