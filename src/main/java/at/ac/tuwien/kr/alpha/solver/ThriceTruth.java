package at.ac.tuwien.kr.alpha.solver;

public enum ThriceTruth {
	TRUE("T", true),
	FALSE("F", false),
	MBT("M", true);

	private final String asString;
	private final boolean asBoolean;

	ThriceTruth(String asString, boolean asBoolean) {
		this.asString = asString;
		this.asBoolean = asBoolean;
	}

	public boolean toBoolean() {
		return asBoolean;
	}

	@Override
	public String toString() {
		return asString;
	}
}