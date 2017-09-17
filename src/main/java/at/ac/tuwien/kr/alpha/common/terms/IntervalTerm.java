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
	private final Term lowerBoundTerm;
	private final Term upperBoundTerm;

	private final int lowerBound;
	private final int upperBound;

	private final boolean ground;

	private IntervalTerm(Term lowerBound, Term upperBound) {
		if (lowerBound == null || upperBound == null) {
			throw new IllegalArgumentException();
		}

		this.ground = !((lowerBound instanceof VariableTerm) || (upperBound instanceof VariableTerm));

		this.lowerBoundTerm = lowerBound;
		this.upperBoundTerm = upperBound;

		if (this.ground) {
			this.upperBound = Integer.parseInt(upperBoundTerm.toString());
			this.lowerBound = Integer.parseInt(lowerBoundTerm.toString());
		} else {
			this.upperBound = -1;
			this.lowerBound = -1;
		}
	}

	public static IntervalTerm getInstance(Term lowerBound, Term upperBound) {
		return new IntervalTerm(lowerBound, upperBound);
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}

	public int getLowerBound() {
		if (!isGround()) {
			throw new RuntimeException("Cannot get the lower bound of non-ground interval. Should not happen.");
		}
		return this.lowerBound;
	}

	public int getUpperBound() {
		if (!isGround()) {
			throw new RuntimeException("Cannot get the upper bound of non-ground interval. Should not happen.");
		}
		return this.upperBound;
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
		if (lowerBoundTerm instanceof VariableTerm) {
			variables.add((VariableTerm) lowerBoundTerm);
		}
		if (upperBoundTerm instanceof VariableTerm) {
			variables.add((VariableTerm) upperBoundTerm);
		}
		return variables;
	}

	@Override
	public Term substitute(Substitution substitution) {
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

		if (!lowerBoundTerm.equals(that.lowerBoundTerm)) {
			return false;
		}
		return upperBoundTerm.equals(that.upperBoundTerm);

	}

	@Override
	public int hashCode() {
		int result = lowerBoundTerm.hashCode();
		result = 31 * result + upperBoundTerm.hashCode();
		return result;
	}

	@Override
	public int compareTo(Term o) {
		throw new RuntimeException("Intervals cannot be compared.");
	}

}
