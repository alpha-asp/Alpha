package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;

public class RegressionTestConfig {

	private final String solverName;

	private final String grounderName;

	private final String noGoodStoreName;

	private final BranchingHeuristicFactory.Heuristic branchingHeuristic;

	private final long seed;

	private final boolean debugChecks;

	private final String grounderToleranceConstraints;

	private final String grounderToleranceRules;

	private final boolean disableInstanceRemoval;

	private final boolean evaluateStratifiedPart;

	public RegressionTestConfig(
			String solverName, String grounderName, String noGoodStoreName,
			BranchingHeuristicFactory.Heuristic branchingHeuristic, long seed,
			boolean debugChecks, String grounderToleranceConstraints, String grounderToleranceRules,
			boolean disableInstanceRemoval, boolean evaluateStratifiedPart) {
		this.solverName = solverName;
		this.grounderName = grounderName;
		this.noGoodStoreName = noGoodStoreName;
		this.branchingHeuristic = branchingHeuristic;
		this.seed = seed;
		this.debugChecks = debugChecks;
		this.grounderToleranceConstraints = grounderToleranceConstraints;
		this.grounderToleranceRules = grounderToleranceRules;
		this.disableInstanceRemoval = disableInstanceRemoval;
		this.evaluateStratifiedPart = evaluateStratifiedPart;
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

	public BranchingHeuristicFactory.Heuristic getBranchingHeuristic() {
		return this.branchingHeuristic;
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

}
