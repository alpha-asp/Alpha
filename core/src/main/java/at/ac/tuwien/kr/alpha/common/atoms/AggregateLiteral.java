package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class AggregateLiteral extends Literal {
	public AggregateLiteral(AggregateAtom atom, boolean positive) {
		super(atom, positive);
	}

	@Override
	public AggregateAtom getAtom() {
		return (AggregateAtom)atom;
	}

	@Override
	public AggregateLiteral negate() {
		return new AggregateLiteral(getAtom(), !positive);
	}

	/**
	 * @see Atom#substitute(Substitution)
	 */
	@Override
	public AggregateLiteral substitute(Substitution substitution) {
		return new AggregateLiteral(getAtom().substitute(substitution), positive);
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		Set<VariableTerm> bindingVariables = new HashSet<>();
		if (boundBindingVariable(getAtom().getLowerBoundOperator(), getAtom().getLowerBoundTerm(), positive) != null) {
			bindingVariables.add((VariableTerm) getAtom().getLowerBoundTerm());
		}
		if (boundBindingVariable(getAtom().getUpperBoundOperator(), getAtom().getUpperBoundTerm(), positive) != null) {
			bindingVariables.add((VariableTerm) getAtom().getUpperBoundTerm());
		}
		return bindingVariables;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		// Note: every local variable that also occurs globally in the rule is a nonBindingVariable, hence an
		// aggregate literal alone cannot detect its non-binding (i.e. global) variables.
		throw new UnsupportedOperationException();
	}

	private static VariableTerm boundBindingVariable(ComparisonOperator op, Term bound, boolean positive) {
		boolean isNormalizedEquality = op == ComparisonOperator.EQ && positive || op == ComparisonOperator.NE && !positive;
		if (isNormalizedEquality &&  bound instanceof VariableTerm) {
			return (VariableTerm) bound;
		}
		return null;
	}

}
