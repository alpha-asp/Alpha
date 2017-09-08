package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An IntervalTerm represents the shorthand notation for a set of rules where all elements in this interval occur once, e.g., fact(2..5).
 * An IntervalTerm is a meta-term and the grounder must replace it with its corresponding set of facts or rules.
 * Copyright (c) 2017, the Alpha Team.
 */
public class IntervalTerm extends Term {

	private final Term lowerBound;
	private final Term upperBound;

	private IntervalTerm(Term lowerBound, Term upperBound) {
		if (lowerBound == null || upperBound == null) {
			throw new IllegalArgumentException();
		}
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public static IntervalTerm getInstance(Term lowerBound, Term upperBound) {
		return new IntervalTerm(lowerBound, upperBound);
	}

	@Override
	public boolean isGround() {
		return !(lowerBound instanceof VariableTerm) && !(upperBound instanceof VariableTerm);
	}

	public int getLowerBound() {
		if (!isGround()) {
			throw new RuntimeException("Cannot get the lower bound of non-ground interval. Should not happen.");
		}
		return Integer.parseInt(lowerBound.toString());
	}

	public int getUpperBound() {
		if (!isGround()) {
			throw new RuntimeException("Cannot get the lower bound of non-ground interval. Should not happen.");
		}
		return Integer.parseInt(upperBound.toString());
	}


	/**
	 * Note: this method always returns an empty list, regardless of the interval containing variables or not.
	 * The reason is that those variables are not binding, even if the interval occurs in a positive atom.
	 * Use getNonBindingVariables() to obtain the list of variables actually occurring in the interval.
	 */
	@Override
	public List<VariableTerm> getBindingVariables() {
		// Variables occurring in an IntervalTerm cannot bind, they must be bound on the outside. In order to not complicate
		return Collections.emptyList();
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		LinkedList<VariableTerm> variables = new LinkedList<>();
		if (lowerBound instanceof VariableTerm) {
			variables.add((VariableTerm) lowerBound);
		}
		if (upperBound instanceof VariableTerm) {
			variables.add((VariableTerm) upperBound);
		}
		return variables;
	}

	@Override
	public Term substitute(Substitution substitution) {
		if (isGround()) {
			return this;
		}
		return new IntervalTerm(lowerBound.substitute(substitution), upperBound.substitute(substitution));
	}

	@Override
	public String toString() {
		return lowerBound + ".." + upperBound;
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

		if (!lowerBound.equals(that.lowerBound)) {
			return false;
		}
		return upperBound.equals(that.upperBound);

	}

	@Override
	public int hashCode() {
		int result = lowerBound.hashCode();
		result = 31 * result + upperBound.hashCode();
		return result;
	}

	@Override
	public int compareTo(Term o) {
		throw new RuntimeException("Intervals cannot be compared.");
	}

}
