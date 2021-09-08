package at.ac.tuwien.kr.alpha.core.solver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.params.provider.Arguments;

import at.ac.tuwien.kr.alpha.api.config.Heuristic;

public class RegressionTestConfigProvider {

	private static final boolean DEFAULT_USE_SORTING_GRID = true;

	private static List<RegressionTestConfig> buildConfigs(boolean useSortingGridForAggregates) {
		// Check whether we are running in a CI environment.
		boolean ci = Boolean.valueOf(System.getenv("CI"));

		//@formatter:off
		String[] solvers = ci ? new String[]{"default", "naive" } : new String[]{"default" };
		String grounder = "naive";
		String[] atomStores = ci ? new String[]{"alpharoaming", "naive" } : new String[]{"alpharoaming" };
		String[] heuristics = ci ? nonDeprecatedHeuristics() : new String[]{"NAIVE", "VSIDS" };
		String[] gtcValues = new String[]{"strict", "permissive" };
		String gtrValue = "strict";
		boolean[] disableInstanceRemovalValues = ci ? new boolean[]{false, true } : new boolean[]{false };
		boolean[] evaluateStratifiedValues = new boolean[]{false, true };
		boolean[] enableDebugChecksValues = new boolean[]{false, true };
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
					for (String grounderTolerance : gtcValues) {
						for (boolean disableInstanceRemoval : disableInstanceRemovalValues) {
							for (boolean evaluateStratified : evaluateStratifiedValues) {
								for (boolean enableDebugChecks : enableDebugChecksValues) {
									configsToTest.add(new RegressionTestConfig(
											solverName, grounder, atomStoreName, Heuristic.valueOf(branchingHeuristicName),
											seed, enableDebugChecks, grounderTolerance, gtrValue, disableInstanceRemoval, evaluateStratified,
											useSortingGridForAggregates));
								}
							}
						}
					}
				}
			}
		}
		return configsToTest;

	}

	public static List<Arguments> provideConfigs() {
		List<Arguments> retVal = new ArrayList<>();
		for (RegressionTestConfig cfg : buildConfigs(DEFAULT_USE_SORTING_GRID)) {
			retVal.add(Arguments.of(cfg));
		}
		return retVal;
	}

	public static List<Arguments> provideAggregateTestConfigs() {
		List<Arguments> retVal = new ArrayList<>();
		for (RegressionTestConfig cfg : buildConfigs(true)) {
			retVal.add(Arguments.of(cfg));
		}
		for (RegressionTestConfig cfg : buildConfigs(false)) {
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
		return nonDeprecatedHeuristicsNames.toArray(new String[] {});
	}
}
