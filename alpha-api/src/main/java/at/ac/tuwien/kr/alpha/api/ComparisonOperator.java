package at.ac.tuwien.kr.alpha.api;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

/**
 * A comparison operator that can be used in {@link ASPCore2Program}s.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ComparisonOperator {

	/**
	 * The operator symbol, i.e. the operator's string representation.
	 */
	String getSymbol();

	/**
	 * The {@link Predicate} associated with this operator.
	 */
	Predicate toPredicate();

	/**
	 * The inverse of this operator (e.g. the inverse of "=" is "!=")
	 */
	ComparisonOperator negate();

	/**
	 * Tests whether two terms are in the relation defined by this operator.
	 */
	boolean compare(Term t1, Term t2);

}
