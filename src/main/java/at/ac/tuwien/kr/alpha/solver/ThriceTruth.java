package at.ac.tuwien.kr.alpha.solver;

public enum ThriceTruth implements Truth {
	TRUE(false),
	FALSE(true),
	MBT(false);

	private final boolean isNegative;

	ThriceTruth(boolean isNegative) {
		this.isNegative = isNegative;
	}

	@Override
	public boolean isNegative() {
		return isNegative;
	}
}