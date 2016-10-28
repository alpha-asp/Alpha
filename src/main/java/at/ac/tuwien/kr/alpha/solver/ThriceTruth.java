package at.ac.tuwien.kr.alpha.solver;

enum ThriceTruth {
	TRUE,
	FALSE,
	MBT;

	public boolean toBoolean() {
		return !FALSE.equals(this);
	}

	public String toStringShorthand() {
		if (TRUE.equals(this)) {
			return "T";
		} else if (FALSE.equals(this)) {
			return "F";
		} else if (MBT.equals(this)) {
			return "M";
		}
		return "Unknown";
	}
}