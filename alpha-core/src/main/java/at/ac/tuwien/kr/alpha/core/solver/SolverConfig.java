package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.core.solver.heuristics.HeuristicsConfiguration;

public class SolverConfig {
	
	private boolean enableDebugChecks;
	private boolean disableJustifications;
	private boolean disableNogoodDeletion;
	private HeuristicsConfiguration heuristicsConfiguration;
	private long randomSeed;
	
	public boolean isEnableDebugChecks() {
		return this.enableDebugChecks;
	}
	
	public void setEnableDebugChecks(boolean enableDebugChecks) {
		this.enableDebugChecks = enableDebugChecks;
	}
	
	public boolean isDisableJustifications() {
		return this.disableJustifications;
	}
	
	public void setDisableJustifications(boolean disableJustifications) {
		this.disableJustifications = disableJustifications;
	}
	
	public boolean isDisableNogoodDeletion() {
		return this.disableNogoodDeletion;
	}
	
	public void setDisableNogoodDeletion(boolean disableNogoodDeletion) {
		this.disableNogoodDeletion = disableNogoodDeletion;
	}
	
	public HeuristicsConfiguration getHeuristicsConfiguration() {
		return this.heuristicsConfiguration;
	}
	
	public void setHeuristicsConfiguration(HeuristicsConfiguration heuristicsConfiguration) {
		this.heuristicsConfiguration = heuristicsConfiguration;
	}
	
	public long getRandomSeed() {
		return this.randomSeed;
	}
	
	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

}
