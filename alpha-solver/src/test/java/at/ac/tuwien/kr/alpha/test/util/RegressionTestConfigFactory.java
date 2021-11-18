package at.ac.tuwien.kr.alpha.test.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.params.provider.Arguments;

import at.ac.tuwien.kr.alpha.api.config.AggregateRewritingConfig;
import at.ac.tuwien.kr.alpha.api.config.Heuristic;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;

public class RegressionTestConfigFactory {

	private static final boolean DEFAULT_EVOLOG_SUPPORT = true;
	private static final String DEFAULT_SOLVER_NAME = "default";
	private static final String DEFAULT_NOGOOD_STORE = "alpharoaming";
	private static final String DEFAULT_BRANCHING_HEURISTIC = "VSIDS";
	private static final String DEFAULT_GROUNDER_TOLERANCE = "strict";
	private static final boolean DEFAULT_DISABLE_INSTANCE_REMOVAL = false;
	private static final boolean DEFAULT_ENABLE_DEBUG_CHECKS = false;

	/**
	 * Creates a list of {@link SystemConfig}s with all config combinations that are to be tested im methods tagged using
	 * "RegressionTest" annotation.
	 * Exact number of combinations depends on the "CI" environment variable that can be used to signal that a test is being run in a CI
	 * environment.
	 * 
	 * @return
	 */
	private static List<SystemConfig> buildConfigs() {
		// Check whether we are running in a CI environment.
		boolean ci = Boolean.valueOf(System.getenv("CI"));

		//@formatter:off
		boolean[] enableEvologSupportValues = new boolean[]{true, false };
		String[] solvers = ci ? new String[]{DEFAULT_SOLVER_NAME, "naive" } : new String[]{DEFAULT_SOLVER_NAME };
		String[] nogoodStores = ci ? new String[]{DEFAULT_NOGOOD_STORE, "naive" } : new String[]{DEFAULT_NOGOOD_STORE };
		String[] heuristics = ci ? nonDeprecatedHeuristics() : new String[]{"NAIVE", DEFAULT_BRANCHING_HEURISTIC };
		String[] grounderTolerancesConstraints = new String[]{DEFAULT_GROUNDER_TOLERANCE, "permissive" };
		String grounderToleranceRules = DEFAULT_GROUNDER_TOLERANCE;
		boolean[] disableInstanceRemovalValues = ci ? new boolean[]{DEFAULT_DISABLE_INSTANCE_REMOVAL, true } : new boolean[]{DEFAULT_DISABLE_INSTANCE_REMOVAL };
		boolean[] evaluateStratifiedValues = new boolean[]{false, true };
		boolean[] enableDebugChecksValues = new boolean[]{DEFAULT_ENABLE_DEBUG_CHECKS, true };
		//@formatter:on

		// NOTE:
		// It is handy to set the seed for reproducing bugs. However, the reverse is also sometimes needed:
		// A test case fails, now what was the seed that "caused" it? To allow this, we need full control over
		// the seed, so we generate one in any case.
		// If your test case fails you can inspect the property called "seed" of AbstractSolverTests and extract
		// its value.
		String seedProperty = System.getProperty("seed", ci ? "0" : "");
		long seed = seedProperty.isEmpty() ? (new Random().nextLong()) : Long.valueOf(seedProperty);

		List<SystemConfig> configsToTest = new ArrayList<>();
		for (boolean enableEvolog : enableEvologSupportValues) {
			for (String solverName : solvers) {
				for (String nogoodStoreName : nogoodStores) {
					for (String branchingHeuristicName : heuristics) {
						for (String grounderToleranceConstraints : grounderTolerancesConstraints) {
							for (boolean disableInstanceRemoval : disableInstanceRemovalValues) {
								for (boolean evaluateStratified : evaluateStratifiedValues) {
									for (boolean enableDebugChecks : enableDebugChecksValues) {
										SystemConfig cfg = new SystemConfig();
										cfg.setAcceptEvologPrograms(enableEvolog);
										cfg.setSolverName(solverName);
										cfg.setNogoodStoreName(nogoodStoreName);
										cfg.setBranchingHeuristicName(branchingHeuristicName);
										cfg.setSeed(seed);
										cfg.setDebugInternalChecks(enableDebugChecks);
										cfg.setGrounderToleranceRules(grounderToleranceRules);
										cfg.setGrounderToleranceConstraints(grounderToleranceConstraints);
										cfg.setGrounderAccumulatorEnabled(disableInstanceRemoval);
										cfg.setEvaluateStratifiedPart(evaluateStratified);
										cfg.setAggregateRewritingConfig(new AggregateRewritingConfig());
										configsToTest.add(cfg);
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
	private static List<SystemConfig> buildConfigsForAggregateTests() {
		List<SystemConfig> configsToTest = new ArrayList<>();

		boolean[] evaluateStratifiedValues = new boolean[] { true, false };
		boolean[] useSortingGridValues = new boolean[] { true, false };
		boolean[] supportNegativeSumElementsValues = new boolean[] { true, false };

		for (boolean evalStratified : evaluateStratifiedValues) {
			for (boolean useSortingGrid : useSortingGridValues) {
				for (boolean supportNegativeElements : supportNegativeSumElementsValues) {
					SystemConfig cfg = new SystemConfig();
					cfg.setAcceptEvologPrograms(DEFAULT_EVOLOG_SUPPORT);
					cfg.setSolverName(DEFAULT_SOLVER_NAME);
					cfg.setNogoodStoreName(DEFAULT_NOGOOD_STORE);
					cfg.setBranchingHeuristicName(DEFAULT_BRANCHING_HEURISTIC);
					cfg.setSeed(0);
					cfg.setDebugInternalChecks(DEFAULT_ENABLE_DEBUG_CHECKS);
					cfg.setGrounderToleranceRules(DEFAULT_GROUNDER_TOLERANCE);
					cfg.setGrounderToleranceConstraints(DEFAULT_GROUNDER_TOLERANCE);
					cfg.setGrounderAccumulatorEnabled(DEFAULT_DISABLE_INSTANCE_REMOVAL);
					cfg.setEvaluateStratifiedPart(evalStratified);
					AggregateRewritingConfig aggregateRewritingConfig = new AggregateRewritingConfig();
					aggregateRewritingConfig.setSupportNegativeValuesInSums(supportNegativeElements);
					aggregateRewritingConfig.setUseSortingGridEncoding(useSortingGrid);
					cfg.setAggregateRewritingConfig(aggregateRewritingConfig);
					configsToTest.add(cfg);
				}
			}
		}

		return configsToTest;
	}

	public static List<Arguments> provideConfigs() {
		List<Arguments> retVal = new ArrayList<>();
		for (SystemConfig cfg : buildConfigs()) {
			retVal.add(Arguments.of(cfg));
		}
		return retVal;
	}

	public static List<Arguments> provideAggregateTestConfigs() {
		List<Arguments> retVal = new ArrayList<>();
		for (SystemConfig cfg : buildConfigsForAggregateTests()) {
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
