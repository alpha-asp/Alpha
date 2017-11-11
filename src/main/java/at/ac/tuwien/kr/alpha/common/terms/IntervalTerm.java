package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.LinkedList;
import java.util.List;

/**
 * An IntervalTerm represents the shorthand notation for a set of rules where all elements in this interval occur once, e.g., fact(2..5).
 * An IntervalTerm is a meta-term and the grounder must replace it with its corresponding set of facts or rules.
 * Copyright (c) 2017, the Alpha Team.
 */
public class IntervalTerm extends Term {
	private final Term lowerBoundTerm;
	private final Term upperBoundTerm;

	private final boolean ground;

	private IntervalTerm(Term lowerBound, Term upperBound) {
		if (lowerBound == null || upperBound == null) {
			throw new IllegalArgumentException();
		}

		this.ground = lowerBound.isGround() && upperBound.isGround();

		this.lowerBoundTerm = lowerBound;
		this.upperBoundTerm = upperBound;
	}

	public static IntervalTerm getInstance(Term lowerBound, Term upperBound) {
		return new IntervalTerm(lowerBound, upperBound);
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}

	public Term getLowerBound() {
		if (!isGround()) {
			throw new RuntimeException("Cannot get the lower bound of non-ground interval. Should not happen.");
		}
		return this.lowerBoundTerm;
	}

	public Term getUpperBound() {
		if (!isGround()) {
			throw new RuntimeException("Cannot get the upper bound of non-ground interval. Should not happen.");
		}
		return this.upperBoundTerm;
	}

	@Override
	public List<Variable> getOccurringVariables() {
		LinkedList<Variable> variables = new LinkedList<>();
		if (lowerBoundTerm instanceof Variable) {
			variables.add((Variable) lowerBoundTerm);
		}
		if (upperBoundTerm instanceof Variable) {
			variables.add((Variable) upperBoundTerm);
		}
		return variables;
	}

	@Override
	public IntervalTerm substitute(Substitution substitution) {
		if (isGround()) {
			return this;
		}
		return new IntervalTerm(lowerBoundTerm.substitute(substitution), upperBoundTerm.substitute(substitution));
	}

	@Override
	public String toString() {
		return lowerBoundTerm + ".." + upperBoundTerm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		IntervalTerm that = (IntervalTerm) o;

		return lowerBoundTerm.equals(that.lowerBoundTerm) && upperBoundTerm.equals(that.upperBoundTerm);
	}

	@Override
	public int hashCode() {
		int result = lowerBoundTerm.hashCode();
		result = 31 * result + upperBoundTerm.hashCode();
		return result;
	}

	@Override
	public int compareTo(Term o) {
		throw new UnsupportedOperationException("Intervals cannot be compared.");
	}

	/**
	 * Returns true if the term contains (or is) some IntervalTerm.
	 * @param term the term to test
	 * @return true iff an IntervalTerm occurs in term.
	 */
	public static boolean termContainsIntervalTerm(Term term) {
		if (term instanceof IntervalTerm) {
			return true;
		} else if (term instanceof FunctionTerm) {
			return functionTermContainsIntervals((FunctionTerm) term);
		} else {
			return false;
		}
	}

	public static boolean functionTermContainsIntervals(FunctionTerm functionTerm) {
		// Test whether a function term contains an interval term (recursively).
		for (Term term : functionTerm.getTerms()) {
			if (term instanceof IntervalTerm) {
				return true;
			}
			if (term instanceof FunctionTerm && functionTermContainsIntervals((FunctionTerm) term)) {
				return true;
			}
		}
		return false;
	}


}
