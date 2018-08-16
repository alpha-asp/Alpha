package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.join;
import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class AggregateAtom implements Atom {

	private final ComparisonOperator lowerBoundOperator;
	private final Term lowerBoundTerm;
	private final ComparisonOperator upperBoundOperator;
	private final Term upperBoundTerm;
	private final AGGREGATEFUNCTION aggregatefunction;
	private final List<AggregateElement> aggregateElements;

	public AggregateAtom(ComparisonOperator lowerBoundOperator, Term lowerBoundTerm, ComparisonOperator upperBoundOperator, Term upperBoundTerm, AGGREGATEFUNCTION aggregatefunction, List<AggregateElement> aggregateElements) {
		this.lowerBoundOperator = lowerBoundOperator;
		this.lowerBoundTerm = lowerBoundTerm;
		this.upperBoundOperator = upperBoundOperator;
		this.upperBoundTerm = upperBoundTerm;
		this.aggregatefunction = aggregatefunction;
		this.aggregateElements = aggregateElements;
		if (upperBoundOperator != null || lowerBoundOperator != ComparisonOperator.LE || lowerBoundTerm == null) {
			throw new UnsupportedOperationException("Aggregate construct not yet supported.");
		}
	}

	@Override
	public boolean isGround() {
		for (AggregateElement aggregateElement : aggregateElements) {
			if (!aggregateElement.isGround()) {
				return false;
			}
		}
		if (lowerBoundTerm != null && !lowerBoundTerm.isGround()
			|| upperBoundTerm != null && !upperBoundTerm.isGround()) {
			return false;
		}
		return true;
	}


	@Override
	public AggregateLiteral toLiteral(boolean positive) {
		return new AggregateLiteral(this, positive);
	}

	@Override
	public List<Term> getTerms() {
		throw oops("Aggregate atom cannot report terms.");
	}

	@Override
	public Predicate getPredicate() {
		throw oops("Aggregate atom cannot report predicate.");
	}

	/**
	 * Returns all variables occurring inside the aggregate, between { ... }.
	 * @return each variable occurring in some aggregate element.
	 */
	public List<VariableTerm> getAggregateVariables() {
		List<VariableTerm> occurringVariables = new LinkedList<>();
		for (AggregateElement aggregateElement : aggregateElements) {
			occurringVariables.addAll(aggregateElement.getOccurringVariables());
		}
		return occurringVariables;
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
	public String toString() {
		String lowerBound = lowerBoundTerm == null ? "" : (lowerBoundTerm.toString() + lowerBoundOperator);
		String upperBound = upperBoundTerm == null ? "" : (upperBoundOperator.toString() + upperBoundTerm);
		return lowerBound + "#" + aggregatefunction + "{ " + join("", aggregateElements, "; ", "") + " }" + upperBound;
	}

	@Override
	public int hashCode() {
		int result = lowerBoundOperator != null ? lowerBoundOperator.hashCode() : 0;
		result = 31 * result + (lowerBoundTerm != null ? lowerBoundTerm.hashCode() : 0);
		result = 31 * result + (upperBoundOperator != null ? upperBoundOperator.hashCode() : 0);
		result = 31 * result + (upperBoundTerm != null ? upperBoundTerm.hashCode() : 0);
		result = 31 * result + (aggregateElements != null ? aggregateElements.hashCode() : 0);
		result = 31 * result + (aggregatefunction != null ? aggregatefunction.hashCode() : 0);
		return result;
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

		public boolean isGround() {
			for (Term elementTerm : elementTerms) {
				if (!elementTerm.isGround()) {
					return false;
				}
			}
			for (Literal elementLiteral : elementLiterals) {
				if (!elementLiteral.isGround()) {
					return false;
				}
			}
			return true;
		}

		public List<VariableTerm> getOccurringVariables() {
			List<VariableTerm> occurringVariables = new LinkedList<>();
			for (Term term : elementTerms) {
				if (term instanceof VariableTerm) {
					occurringVariables.add((VariableTerm) term);
				}
			}
			for (Literal literal : elementLiterals) {
				occurringVariables.addAll(literal.getBindingVariables());
				occurringVariables.addAll(literal.getNonBindingVariables());
			}
			return occurringVariables;
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

		@Override
		public String toString() {
			return join("", elementTerms, " : ") + join("", elementLiterals, "");
		}
	}
}
