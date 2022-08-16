package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.api.config.Heuristic;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;

public class RegressionTestConfig {

	private final String solverName;

	private final String grounderName;

	private final String noGoodStoreName;

	private final Heuristic branchingHeuristic;

	private final boolean restartsEnabled;
	private final int restartIterations;

	private final long seed;

	private final boolean debugChecks;

	private final String grounderToleranceConstraints;

	private final String grounderToleranceRules;

	private final boolean disableInstanceRemoval;

	private final boolean evaluateStratifiedPart;

	private final boolean encodeAggregatesUsingSortingGrid;
	
	private final boolean supportNegativeSumElements;

	public RegressionTestConfig(
			String solverName, String grounderName, String noGoodStoreName,
			Heuristic branchingHeuristic, boolean restartsEnabled, int restartIterations, long seed,
			boolean debugChecks, String grounderToleranceConstraints, String grounderToleranceRules,
			boolean disableInstanceRemoval, boolean evaluateStratifiedPart, boolean useSortingGrid,
			boolean supportNegativeSumElements) {
		this.solverName = solverName;
		this.grounderName = grounderName;
		this.noGoodStoreName = noGoodStoreName;
		this.branchingHeuristic = branchingHeuristic;
		this.restartsEnabled = restartsEnabled;
		this.restartIterations = restartIterations;
		this.seed = seed;
		this.debugChecks = debugChecks;
		this.grounderToleranceConstraints = grounderToleranceConstraints;
		this.grounderToleranceRules = grounderToleranceRules;
		this.disableInstanceRemoval = disableInstanceRemoval;
		this.evaluateStratifiedPart = evaluateStratifiedPart;
		this.encodeAggregatesUsingSortingGrid = useSortingGrid;
		this.supportNegativeSumElements = supportNegativeSumElements;
	}

	public SystemConfig toSystemConfig() {
		SystemConfig retVal = new SystemConfig();
		retVal.setGrounderName(this.grounderName);
		retVal.setSolverName(this.solverName);
		retVal.setNogoodStoreName(this.noGoodStoreName);
		retVal.setRestartsEnabled(this.restartsEnabled);
		retVal.setRestartIterations(this.restartIterations);
		retVal.setSeed(this.seed);
		retVal.setBranchingHeuristic(this.branchingHeuristic);
		retVal.setDebugInternalChecks(this.debugChecks);
		retVal.setEvaluateStratifiedPart(this.evaluateStratifiedPart);
		retVal.setGrounderAccumulatorEnabled(this.disableInstanceRemoval);
		retVal.setGrounderToleranceRules(this.grounderToleranceRules);
		retVal.setGrounderToleranceConstraints(this.grounderToleranceConstraints);
		retVal.getAggregateRewritingConfig().setUseSortingGridEncoding(this.encodeAggregatesUsingSortingGrid);
		retVal.getAggregateRewritingConfig().setSupportNegativeValuesInSums(this.supportNegativeSumElements);
		return retVal;
	}

	public String getSolverName() {
		return this.solverName;
	}

	public String getGrounderName() {
		return this.grounderName;
	}

	public String getNoGoodStoreName() {
		return this.noGoodStoreName;
	}

	public Heuristic getBranchingHeuristic() {
		return this.branchingHeuristic;
	}

	public boolean isRestartsEnabled() {
		return restartsEnabled;
	}

	public int getRestartIterations() {
		return restartIterations;
	}

	public long getSeed() {
		return this.seed;
	}

	public boolean isDebugChecks() {
		return this.debugChecks;
	}

	public String getGrounderToleranceConstraints() {
		return this.grounderToleranceConstraints;
	}

	public String getGrounderToleranceRules() {
		return this.grounderToleranceRules;
	}

	public boolean isDisableInstanceRemoval() {
		return this.disableInstanceRemoval;
	}

	public boolean isEvaluateStratifiedPart() {
		return this.evaluateStratifiedPart;
	}

	public boolean isEncodeAggregatesUsingSortingGrid() {
		return this.encodeAggregatesUsingSortingGrid;
	}

	public boolean isSupportNegativeSumElements() {
		return this.supportNegativeSumElements;
	}
	
	@Override
	public String toString() {
		return String.format(
				"RegressionTestConfig [solverName=%s, grounderName=%s, noGoodStoreName=%s, branchingHeuristic=%s,"
				+ " restartsEnabled=%b, restartIterations=%d, seed=%s, debugChecks=%s, grounderToleranceConstraints=%s,"
				+ " grounderToleranceRules=%s, disableInstanceRemoval=%s, evaluateStratifiedPart=%s, useSortingGrid=%s,"
				+ " supportNegativeSumElements=%s]",
				this.solverName, this.grounderName, this.noGoodStoreName, this.branchingHeuristic, this.restartsEnabled,
				this.restartIterations, this.seed, this.debugChecks, this.grounderToleranceConstraints,
				this.grounderToleranceRules, this.disableInstanceRemoval, this.evaluateStratifiedPart,
				this.encodeAggregatesUsingSortingGrid, this.supportNegativeSumElements);
	}
}
