package at.ac.tuwien.kr.alpha.api.terms;

/**
 * A term representing an interval of integers.
 * Intervals are syntactic sugar, e.g. the rule "p(X) :- X = 1..2." is a shorthand for
 * "p(X) :- X = 1. p(X) :- X = 2.".
 */
public interface IntervalTerm extends Term {

	Term getLowerBound();

	Term getUpperBound();

}
