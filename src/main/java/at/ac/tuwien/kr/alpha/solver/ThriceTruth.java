package at.ac.tuwien.kr.alpha.solver;

public enum ThriceTruth implements Truth {
	TRUE(true),
	FALSE(false),
	MBT(true);

	private final boolean containedIn;

	ThriceTruth(boolean containedIn) {
		this.containedIn = containedIn;
	}

	@Override
	public boolean matches(boolean negated) {
		return containedIn == negated;
	}
}