package at.ac.tuwien.kr.alpha.config;

import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory.Heuristic;

public class AlphaConfig {

	// Note: Defining constants for default values here rather than just
	// initializing from those values in order to have the values accessible in
	// contexts where no AlphaConfig instance exists (e.g. argument parsing from
	// command line)
	public static final String DEFAULT_GROUNDER_NAME = "naive";
	public static final String DEFAULT_SOLVER_NAME = "default";
	public static final String DEFAULT_NOGOOD_STORE_NAME = "alphaRoaming";
	public static final String DEFAULT_BRANCHING_HEURISTIC_NAME = Heuristic.NAIVE.name();
	public static final long DEFAULT_SEED = System.nanoTime();
	public static final boolean DEFAULT_DETERMINISTIC = false;
	public static final boolean DEFAULT_PRINT_STATS = false;
	public static final boolean DEFAULT_QUIET = false;
	public static final boolean DEFAULT_DISABLE_JUSTIFICATION_SEARCH = false;
	public static final boolean DEFAULT_DEBUG_INTERNAL_CHECKS = false;
	public static final boolean DEFAULT_USE_NORMALIZATION_GRID = false;
	public static final boolean DEFAULT_SORT_ANSWER_SETS = false;

	private String grounderName = AlphaConfig.DEFAULT_GROUNDER_NAME;
	private String solverName = AlphaConfig.DEFAULT_SOLVER_NAME;
	private String nogoodStoreName = AlphaConfig.DEFAULT_NOGOOD_STORE_NAME;
	private boolean deterministic = AlphaConfig.DEFAULT_DETERMINISTIC;
	private long seed = AlphaConfig.DEFAULT_SEED;
	private boolean debugInternalChecks = AlphaConfig.DEFAULT_DEBUG_INTERNAL_CHECKS;
	private String branchingHeuristicName = AlphaConfig.DEFAULT_BRANCHING_HEURISTIC_NAME;
	private boolean quiet = AlphaConfig.DEFAULT_QUIET;
	private boolean printStats = AlphaConfig.DEFAULT_PRINT_STATS;
	private boolean disableJustificationSearch = AlphaConfig.DEFAULT_DISABLE_JUSTIFICATION_SEARCH;
	private boolean useNormalizationGrid = AlphaConfig.DEFAULT_USE_NORMALIZATION_GRID;
	private boolean sortAnswerSets = AlphaConfig.DEFAULT_SORT_ANSWER_SETS;

	public String getGrounderName() {
		return this.grounderName;
	}

	public void setGrounderName(String grounderName) {
		this.grounderName = grounderName;
	}

	public String getSolverName() {
		return this.solverName;
	}

	public void setSolverName(String solverName) {
		this.solverName = solverName;
	}

	public String getNogoodStoreName() {
		return this.nogoodStoreName;
	}

	public void setNogoodStoreName(String nogoodStoreName) {
		this.nogoodStoreName = nogoodStoreName;
	}

	public boolean isDeterministic() {
		return this.deterministic;
	}

	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}

	public long getSeed() {
		return this.seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public boolean isDebugInternalChecks() {
		return this.debugInternalChecks;
	}

	public void setDebugInternalChecks(boolean debugInternalChecks) {
		this.debugInternalChecks = debugInternalChecks;
	}

	public String getBranchingHeuristicName() {
		return this.branchingHeuristicName;
	}

	public void setBranchingHeuristicName(String branchingHeuristicName) {
		this.branchingHeuristicName = branchingHeuristicName;
	}

	public boolean isQuiet() {
		return this.quiet;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	public boolean isPrintStats() {
		return this.printStats;
	}

	public void setPrintStats(boolean printStats) {
		this.printStats = printStats;
	}

	public boolean isDisableJustificationSearch() {
		return this.disableJustificationSearch;
	}

	public void setDisableJustificationSearch(boolean disableJustificationSearch) {
		this.disableJustificationSearch = disableJustificationSearch;
	}

	public boolean isUseNormalizationGrid() {
		return this.useNormalizationGrid;
	}

	public void setUseNormalizationGrid(boolean useNormalizationGrid) {
		this.useNormalizationGrid = useNormalizationGrid;
	}

	public boolean isSortAnswerSets() {
		return this.sortAnswerSets;
	}

	public void setSortAnswerSets(boolean sortAnswerSets) {
		this.sortAnswerSets = sortAnswerSets;
	}

}
