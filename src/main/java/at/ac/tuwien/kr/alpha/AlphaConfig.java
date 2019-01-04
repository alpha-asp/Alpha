package at.ac.tuwien.kr.alpha;

import java.io.InputStream;

public class AlphaConfig {

	public static final String DEFAULT_GROUNDER_NAME = "naive";
	public static final String DEFAULT_SOLVER_NAME = "default";
	public static final String DEFAULT_NOGOOD_STORE_NAME = "alphaRoaming";

	private int numAnswerSets;
	private InputStream input;
	private String grounderName;
	private String solverName;
	private String nogoodStoreName;
	private String[] filters;
	private boolean sortAnswerSets;
	private boolean deterministic;
	private int seed;
	private boolean debugInternalChecks;
	private String branchingHeuristicName;
	private boolean quiet;
	private boolean literate;
	private boolean printStats;
	private boolean disableJustificationSearch;
	private boolean useNormalizationGrid;

	public int getNumAnswerSets() {
		return this.numAnswerSets;
	}

	public void setNumAnswerSets(int numAnswerSets) {
		this.numAnswerSets = numAnswerSets;
	}

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

	public String[] getFilters() {
		return this.filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
	}

	public boolean isSortAnswerSets() {
		return this.sortAnswerSets;
	}

	public void setSortAnswerSets(boolean sortAnswerSets) {
		this.sortAnswerSets = sortAnswerSets;
	}

	public boolean isDeterministic() {
		return this.deterministic;
	}

	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}

	public int getSeed() {
		return this.seed;
	}

	public void setSeed(int seed) {
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

	public boolean isLiterate() {
		return this.literate;
	}

	public void setLiterate(boolean literate) {
		this.literate = literate;
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

	public InputStream getInput() {
		return this.input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}

}
