package at.ac.tuwien.kr.alpha.core.common;

import at.ac.tuwien.kr.alpha.api.Util;

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
		throw Util.oops("Unknown binary operator encountered, cannot negate it");
	}
	
	public CorePredicate predicate() {
		return CorePredicate.getInstance(this.asString, 2);
	}
}
