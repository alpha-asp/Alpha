package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

public enum BooleanTruth implements Truth {
	TRUE(true),
	FALSE(false);

	private final boolean asBoolean;

	BooleanTruth(boolean asBoolean) {
		this.asBoolean = asBoolean;
	}

	public static BooleanTruth valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}

	@Override
	public boolean toBoolean() {
		return asBoolean;
	}

	@Override
	public boolean isBoolean() {
		return true;
	}
}
