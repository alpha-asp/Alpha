package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.BooleanTruth;

enum ThriceTruth implements BooleanTruth {
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

	@Override
	public boolean isFalse() {
		return this.equals(FALSE);
	}
}