package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.LinkedList;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class AggregateAtom implements Literal {

	public static class AggregateElement {
		public final List<Term> elementTerms;
		public final List<Literal> elementLiterals;

		public AggregateElement(List<Term> elementTerms, List<Literal> elementLiterals) {
			this.elementTerms = elementTerms;
			this.elementLiterals = elementLiterals;
		}
	}

	private final boolean negated;
	private final ComparisonOperator lowerBoundOperator;
	private final Term lowerBoundTerm;
	private final ComparisonOperator upperBoundOperator;
	private final Term upperBoundTerm;
	private final List<AggregateElement> aggregateElements;

	public AggregateAtom(boolean negated, ComparisonOperator lowerBoundOperator, Term lowerBoundTerm, ComparisonOperator upperBoundOperator, Term upperBoundTerm, List<AggregateElement> aggregateElements) {
		this.negated = negated;
		this.lowerBoundOperator = lowerBoundOperator;
		this.lowerBoundTerm = lowerBoundTerm;
		this.upperBoundOperator = upperBoundOperator;
		this.upperBoundTerm = upperBoundTerm;
		this.aggregateElements = aggregateElements;
		// TODO: add defaults if some bound is not given!
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public Predicate getPredicate() {
		throw oops("Aggregates have no predicate.");
	}

	@Override
	public List<Term> getTerms() {
		throw oops("Aggregates have no predicate.");
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

		}
		return nonBindingVariables;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return null;
	}
}
