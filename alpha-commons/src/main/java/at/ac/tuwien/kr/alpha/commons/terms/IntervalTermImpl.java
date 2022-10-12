package at.ac.tuwien.kr.alpha.commons.terms;

import java.util.LinkedHashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.util.Interner;

/**
 * An IntervalTerm represents the shorthand notation for a set of rules where all elements in this interval occur once, e.g., fact(2..5).
 * An IntervalTerm is a meta-term and the grounder must replace it with its corresponding set of facts or rules.
 * Copyright (c) 2017, the Alpha Team.
 */
class IntervalTermImpl extends AbstractTerm implements IntervalTerm {
	
	private static final Interner<IntervalTermImpl> INTERNER = new Interner<>();
	private final Term lowerBoundTerm;
	private final Term upperBoundTerm;

	private final boolean ground;

	private IntervalTermImpl(Term lowerBound, Term upperBound) {
		if (lowerBound == null || upperBound == null) {
			throw new IllegalArgumentException();
		}

		this.ground = !((lowerBound instanceof VariableTerm) || (upperBound instanceof VariableTerm));

		this.lowerBoundTerm = lowerBound;
		this.upperBoundTerm = upperBound;
	}

	static IntervalTermImpl getInstance(Term lowerBound, Term upperBound) {
		return INTERNER.intern(new IntervalTermImpl(lowerBound, upperBound));
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}

	public Term getLowerBound() {
		return lowerBoundTerm;
	}

	public Term getUpperBound() {
		return upperBoundTerm;
	}

	@Override
	public Set<VariableTerm> getOccurringVariables() {
		Set<VariableTerm> variables = new LinkedHashSet<>();
		if (lowerBoundTerm instanceof VariableTerm) {
			variables.add((VariableTermImpl) lowerBoundTerm);
		}
		if (upperBoundTerm instanceof VariableTerm) {
			variables.add((VariableTermImpl) upperBoundTerm);
		}
		return variables;
	}

	@Override
	public IntervalTermImpl substitute(Substitution substitution) {
		if (isGround()) {
			return this;
		}
		return new IntervalTermImpl(lowerBoundTerm.substitute(substitution), upperBoundTerm.substitute(substitution));
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

		IntervalTermImpl that = (IntervalTermImpl) o;

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
	public int compareTo(Term o) {
		throw new UnsupportedOperationException("Intervals cannot be compared.");
	}

	@Override
	public Term renameVariables(String renamePrefix) {
		return new IntervalTermImpl(lowerBoundTerm.renameVariables(renamePrefix), upperBoundTerm.renameVariables(renamePrefix));
	}

	@Override
	public Term normalizeVariables(String renamePrefix, Term.RenameCounter counter) {
		return IntervalTermImpl.getInstance(
			lowerBoundTerm.normalizeVariables(renamePrefix, counter),
			upperBoundTerm.normalizeVariables(renamePrefix, counter));
	}

}