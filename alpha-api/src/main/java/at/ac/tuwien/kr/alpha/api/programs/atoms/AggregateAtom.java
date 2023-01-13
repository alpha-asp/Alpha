package at.ac.tuwien.kr.alpha.api.programs.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;

/**
 * An {@link Atom} representing a comparison over an aggregate function, for example '13 < #sum{ X : p(X) }'.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface AggregateAtom extends Atom {

	/**
	 * Aggregate functions supported by Alpha.
	 * 
	 * Copyright (c) 2021, the Alpha Team.
	 */
	enum AggregateFunctionSymbol {
		COUNT,
		MAX,
		MIN,
		SUM
	}

	ComparisonOperator getLowerBoundOperator();

	Term getLowerBoundTerm();

	ComparisonOperator getUpperBoundOperator();

	Term getUpperBoundTerm();

	AggregateFunctionSymbol getAggregateFunction();

	List<VariableTerm> getAggregateVariables();

	List<AggregateElement> getAggregateElements();

	@Override
	AggregateLiteral toLiteral(boolean positive);

	interface AggregateElement {

		List<Term> getElementTerms();

		List<Literal> getElementLiterals();

		List<VariableTerm> getOccurringVariables();

		boolean isGround();

	}

}
