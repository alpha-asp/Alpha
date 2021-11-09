package at.ac.tuwien.kr.alpha.api.terms;

/**
 * A {@link Term} that is the result of an arithmetic calculation.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ArithmeticTerm extends Term {

	ArithmeticOperator getOperator();

	Term getLeftOperand();

	Term getRightOperand();

	Integer evaluateExpression();

}
