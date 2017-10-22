package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.predicates.Evaluable;
import at.ac.tuwien.kr.alpha.common.predicates.BuiltinBiPredicate;

public enum BinaryOperator {
	EQ("="),
	NE("!="),
	LT("<"),
	GT(">"),
	LE("<="),
	GE(">=");

	private String asString;
	private Evaluable asPredicate;

	BinaryOperator(String asString) {
		this.asString = asString;
		this.asPredicate = new BuiltinBiPredicate(this);
	}

	@Override
	public String toString() {
		return asString;
	}

	public Evaluable toPredicate() {
		return asPredicate;
	}

	public BinaryOperator getNegation() {
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
