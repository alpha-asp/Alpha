package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class AggregateAtom implements BodyElement {

	private final boolean negated;
	private final ComparisonOperator lowerBoundOperator;
	private final Term lowerBoundTerm;
	private final ComparisonOperator upperBoundOperator;
	private final Term upperBoundTerm;
	private final AGGREGATEFUNCTION aggregatefunction;
	private final List<AggregateElement> aggregateElements;

	public AggregateAtom(boolean negated, ComparisonOperator lowerBoundOperator, Term lowerBoundTerm, ComparisonOperator upperBoundOperator, Term upperBoundTerm, AGGREGATEFUNCTION aggregatefunction, List<AggregateElement> aggregateElements) {
		this.negated = negated;
		this.lowerBoundOperator = lowerBoundOperator;
		this.lowerBoundTerm = lowerBoundTerm;
		this.upperBoundOperator = upperBoundOperator;
		this.upperBoundTerm = upperBoundTerm;
		this.aggregatefunction = aggregatefunction;
		this.aggregateElements = aggregateElements;
		if (upperBoundOperator != null || lowerBoundOperator != ComparisonOperator.LE) {
			throw new UnsupportedOperationException("Aggregate construct not yet supported.");
		}
		// TODO: add defaults if some bound is not given!
	}

	@Override
	public boolean isGround() {
		for (AggregateElement aggregateElement : aggregateElements) {
			for (Term elementTerm : aggregateElement.elementTerms) {
				if (!elementTerm.isGround()) {
					return false;
				}
			}
			for (Literal elementLiteral : aggregateElement.elementLiterals) {
				if (!elementLiteral.isGround()) {
					return false;
				}
			}
		}
		if (lowerBoundTerm != null && !lowerBoundTerm.isGround()
			|| upperBoundTerm != null && !upperBoundTerm.isGround()) {
			return false;
		}
		return true;
	}

	private VariableTerm boundBindingVariable(ComparisonOperator op, Term bound, boolean isNegated) {
		boolean isNormalizedEquality = op == ComparisonOperator.EQ && !isNegated || op == ComparisonOperator.NE && isNegated;
		if (isNormalizedEquality &&  bound instanceof VariableTerm) {
			return (VariableTerm) bound;
		}
		return null;
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		List<VariableTerm> bindingVariables = new LinkedList<>();
		if (boundBindingVariable(lowerBoundOperator, lowerBoundTerm, negated) != null) {
			bindingVariables.add((VariableTerm) lowerBoundTerm);
		}
		if (boundBindingVariable(upperBoundOperator, upperBoundTerm, negated) != null) {
			bindingVariables.add((VariableTerm) upperBoundTerm);
		}
		return bindingVariables;
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		// TODO: every local variable that also occurs globally in the rule is a nonBindingVariable.
		// TODO: the element conditions must be locally safe
		// TODO: need a notion of variable global in a rule.
		// TODO: collect all occurring variables but only report the global ones
		List<VariableTerm> nonBindingVariables = new LinkedList<>();
		for (AggregateElement aggregateElement : aggregateElements) {
			nonBindingVariables = null; // TODO: remove.
		}
		return nonBindingVariables;
	}

	@Override
	public AggregateAtom substitute(Substitution substitution) {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AggregateAtom that = (AggregateAtom) o;

		if (negated != that.negated) {
			return false;
		}
		if (lowerBoundOperator != that.lowerBoundOperator) {
			return false;
		}
		if (lowerBoundTerm != null ? !lowerBoundTerm.equals(that.lowerBoundTerm) : that.lowerBoundTerm != null) {
			return false;
		}
		if (upperBoundOperator != that.upperBoundOperator) {
			return false;
		}
		if (upperBoundTerm != null ? !upperBoundTerm.equals(that.upperBoundTerm) : that.upperBoundTerm != null) {
			return false;
		}
		if (aggregateElements != null ? !aggregateElements.equals(that.aggregateElements) : that.aggregateElements != null) {
			return false;
		}
		return aggregatefunction == that.aggregatefunction;
	}

	@Override
	public int hashCode() {
		int result = negated ? 1 : 0;
		result = 31 * result + (lowerBoundOperator != null ? lowerBoundOperator.hashCode() : 0);
		result = 31 * result + (lowerBoundTerm != null ? lowerBoundTerm.hashCode() : 0);
		result = 31 * result + (upperBoundOperator != null ? upperBoundOperator.hashCode() : 0);
		result = 31 * result + (upperBoundTerm != null ? upperBoundTerm.hashCode() : 0);
		result = 31 * result + (aggregateElements != null ? aggregateElements.hashCode() : 0);
		result = 31 * result + (aggregatefunction != null ? aggregatefunction.hashCode() : 0);
		return result;
	}

	public boolean isNegated() {
		return negated;
	}

	public ComparisonOperator getLowerBoundOperator() {
		return lowerBoundOperator;
	}

	public Term getLowerBoundTerm() {
		return lowerBoundTerm;
	}

	public ComparisonOperator getUpperBoundOperator() {
		return upperBoundOperator;
	}

	public Term getUpperBoundTerm() {
		return upperBoundTerm;
	}

	public AGGREGATEFUNCTION getAggregatefunction() {
		return aggregatefunction;
	}

	public List<AggregateElement> getAggregateElements() {
		return Collections.unmodifiableList(aggregateElements);
	}

	public enum AGGREGATEFUNCTION {
		COUNT,
		MAX,
		MIN,
		SUM
	}

	public static class AggregateElement {
		final List<Term> elementTerms;
		final List<Literal> elementLiterals;

		public AggregateElement(List<Term> elementTerms, List<Literal> elementLiterals) {
			this.elementTerms = elementTerms;
			this.elementLiterals = elementLiterals;
		}

		public List<Term> getElementTerms() {
			return elementTerms;
		}

		public List<Literal> getElementLiterals() {
			return elementLiterals;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			AggregateElement that = (AggregateElement) o;

			if (elementTerms != null ? !elementTerms.equals(that.elementTerms) : that.elementTerms != null) {
				return false;
			}
			return elementLiterals != null ? elementLiterals.equals(that.elementLiterals) : that.elementLiterals == null;
		}

		@Override
		public int hashCode() {
			int result = elementTerms != null ? elementTerms.hashCode() : 0;
			result = 31 * result + (elementLiterals != null ? elementLiterals.hashCode() : 0);
			return result;
		}
	}
}
