package at.ac.tuwien.kr.alpha.api.programs.literals;

import java.util.function.Function;

public interface ComparisonLiteral extends FixedInterpretationLiteral {

	boolean isLeftOrRightAssigning();

	boolean isNormalizedEquality();

	ComparisonLiteral renameVariables(Function<String, String> mapping);

}
