package at.ac.tuwien.kr.alpha.solver;

enum ThriceTruth {
	TRUE,
	FALSE,
	MBT;

	public boolean toBoolean() {
		return !FALSE.equals(this);
	}
}