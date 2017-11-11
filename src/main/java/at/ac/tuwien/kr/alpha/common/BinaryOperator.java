package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.interpretations.BuiltinBiPredicate;
import at.ac.tuwien.kr.alpha.common.interpretations.FixedInterpretation;
import at.ac.tuwien.kr.alpha.common.symbols.Predicate;

public enum BinaryOperator {
	EQ("="),
	NE("!="),
	LT("<"),
	GT(">"),
	LE("<="),
	GE(">=");

	private String asString;
	private FixedInterpretation interpretation;

	BinaryOperator(String asString) {
		this.asString = asString;
		this.interpretation = new BuiltinBiPredicate(this);
	}

	@Override
	public String toString() {
		return asString;
	}

	public FixedInterpretation getInterpretation() {
		return interpretation;
	}

	public Predicate toPredicate() {
		return new Predicate(asString, 2);
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
