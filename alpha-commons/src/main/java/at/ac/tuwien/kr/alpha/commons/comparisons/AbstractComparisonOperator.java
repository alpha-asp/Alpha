package at.ac.tuwien.kr.alpha.commons.comparisons;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.terms.Term;

abstract class AbstractComparisonOperator implements ComparisonOperator {
	
	private final String symbol;
	
	AbstractComparisonOperator(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String getSymbol() {
		return this.symbol;
	}
	
	@Override
	public String toString() {
		return this.getSymbol();
	}

	@Override
	public Predicate toPredicate() {
		return null; // TODO PredicateImpl.getInstance(this.getSymbol(), 2)
	}

	@Override
	public abstract ComparisonOperator negate();

	@Override
	public abstract boolean compare(Term t1, Term t2);

}
