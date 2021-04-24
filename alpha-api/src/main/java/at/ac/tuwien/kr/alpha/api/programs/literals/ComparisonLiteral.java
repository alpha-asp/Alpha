package at.ac.tuwien.kr.alpha.api.programs.literals;

public interface ComparisonLiteral extends ASPCore2Literal, NormalLiteral, FixedInterpretationLiteral {

	boolean isLeftOrRightAssigning();

	boolean isNormalizedEquality();

}
