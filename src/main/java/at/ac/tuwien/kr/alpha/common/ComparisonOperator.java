package at.ac.tuwien.kr.alpha.common;

public enum ComparisonOperator {
	EQ("="),
	NE("!="),
	LT("<"),
	GT(">"),
	LE("<="),
	GE(">=");

	private String asString;

	ComparisonOperator(String asString) {
		this.asString = asString;
	}

	@Override
	public String toString() {
		return asString;
	}

	public ComparisonOperator getNegation() {
		switch (this) {
			case EQ: return NE;
			case NE: return EQ;
			case LT: return GE;
			case GT: return LE;
			case LE: return GT;
			case GE: return LT;
		}
		throw new RuntimeException("Unknown binary operator encountered, cannot negate it.");
	}
}
