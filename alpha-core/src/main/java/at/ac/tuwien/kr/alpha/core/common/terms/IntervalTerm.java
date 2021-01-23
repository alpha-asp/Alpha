package at.ac.tuwien.kr.alpha.core.common.terms;

import at.ac.tuwien.kr.alpha.core.common.Interner;
import at.ac.tuwien.kr.alpha.core.grounder.Substitution;

import static at.ac.tuwien.kr.alpha.core.util.Util.oops;

import java.util.LinkedList;
import java.util.List;

/**
 * An IntervalTerm represents the shorthand notation for a set of rules where all elements in this interval occur once, e.g., fact(2..5).
 * An IntervalTerm is a meta-term and the grounder must replace it with its corresponding set of facts or rules.
 * Copyright (c) 2017, the Alpha Team.
 */
public class IntervalTerm extends CoreTerm {
	private static final Interner<IntervalTerm> INTERNER = new Interner<>();
	private final CoreTerm lowerBoundTerm;
	private final CoreTerm upperBoundTerm;

	private final int lowerBound;
	private final int upperBound;

	private final boolean ground;

	private IntervalTerm(CoreTerm lowerBound, CoreTerm upperBound) {
		if (lowerBound == null || upperBound == null) {
			throw new IllegalArgumentException();
		}

		this.ground = !((lowerBound instanceof VariableTermImpl) || (upperBound instanceof VariableTermImpl));

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

	public static IntervalTerm getInstance(CoreTerm lowerBound, CoreTerm upperBound) {
		return INTERNER.intern(new IntervalTerm(lowerBound, upperBound));
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}

	public int getLowerBound() {
		if (!isGround()) {
			throw oops("Cannot get the lower bound of non-ground interval");
		}
		return this.lowerBound;
	}

	public int getUpperBound() {
		if (!isGround()) {
			throw oops("Cannot get the upper bound of non-ground interval");
		}
		return this.upperBound;
	}

	@Override
	public List<VariableTermImpl> getOccurringVariables() {
		LinkedList<VariableTermImpl> variables = new LinkedList<>();
		if (lowerBoundTerm instanceof VariableTermImpl) {
			variables.add((VariableTermImpl) lowerBoundTerm);
		}
		if (upperBoundTerm instanceof VariableTermImpl) {
			variables.add((VariableTermImpl) upperBoundTerm);
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

		if (!lowerBoundTerm.equals(that.lowerBoundTerm)) {
			return false;
		}
		return upperBoundTerm.equals(that.upperBoundTerm);

	}

	@Override
	public int hashCode() {
		return 31 * lowerBoundTerm.hashCode() + upperBoundTerm.hashCode();
	}

	@Override
	public int compareTo(CoreTerm o) {
		throw new UnsupportedOperationException("Intervals cannot be compared.");
	}

	@Override
	public CoreTerm renameVariables(String renamePrefix) {
		return new IntervalTerm(lowerBoundTerm.renameVariables(renamePrefix), upperBoundTerm.renameVariables(renamePrefix));
	}

	@Override
	public CoreTerm normalizeVariables(String renamePrefix, RenameCounter counter) {
		return IntervalTerm.getInstance(
			lowerBoundTerm.normalizeVariables(renamePrefix, counter),
			upperBoundTerm.normalizeVariables(renamePrefix, counter));
	}

	/**
	 * Returns true if the term contains (or is) some IntervalTerm.
	 * @param term the term to test
	 * @return true iff an IntervalTerm occurs in term.
	 */
	public static boolean termContainsIntervalTerm(CoreTerm term) {
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
		for (CoreTerm term : functionTerm.getTerms()) {
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