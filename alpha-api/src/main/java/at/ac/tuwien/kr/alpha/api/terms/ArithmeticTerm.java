package at.ac.tuwien.kr.alpha.api.terms;

import java.util.function.Function;

/**
 * A {@link Term} that is a binary arithmetic expression.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ArithmeticTerm extends Term {

	ArithmeticOperator getOperator();

	Term getLeftOperand();

	Term getRightOperand();

	Integer evaluateExpression();

	ArithmeticTerm renameVariables(Function<String, String> mapping);

}
