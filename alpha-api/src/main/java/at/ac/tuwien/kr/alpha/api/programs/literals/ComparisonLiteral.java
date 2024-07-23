package at.ac.tuwien.kr.alpha.api.programs.literals;

public interface ComparisonLiteral extends FixedInterpretationLiteral {

	boolean isLeftOrRightAssigning();

	boolean isNormalizedEquality();

}
