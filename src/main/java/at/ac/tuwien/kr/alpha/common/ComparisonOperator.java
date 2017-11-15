package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.interpretations.BuiltinBiPredicate;
import at.ac.tuwien.kr.alpha.common.interpretations.FixedInterpretation;
import at.ac.tuwien.kr.alpha.common.symbols.Predicate;

import static at.ac.tuwien.kr.alpha.Util.oops;

public enum ComparisonOperator {
	EQ("="),
	NE("!="),
	LT("<"),
	GT(">"),
	LE("<="),
	GE(">=");

	private String asString;
	private FixedInterpretation interpretation;

	ComparisonOperator(String asString) {
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
		return Predicate.getInstance(asString, 2);
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
		throw oops("Unknown binary operator encountered, cannot negate it");
	}
}
