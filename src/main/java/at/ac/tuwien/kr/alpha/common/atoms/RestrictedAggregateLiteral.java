package at.ac.tuwien.kr.alpha.common.atoms;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

/**
 * Copyright (c) 2020, the Alpha Team.
 */
public class RestrictedAggregateLiteral extends Literal {
	public RestrictedAggregateLiteral(RestrictedAggregateAtom atom, boolean positive) {
		super(atom, positive);
	}

	@Override
	public RestrictedAggregateAtom getAtom() {
		return (RestrictedAggregateAtom) atom;
	}

	@Override
	public RestrictedAggregateLiteral negate() {
		return new RestrictedAggregateLiteral(getAtom(), !positive);
	}

	/**
	 * @see Atom#substitute(Substitution)
	 */
	@Override
	public RestrictedAggregateLiteral substitute(Substitution substitution) {
		return new RestrictedAggregateLiteral(getAtom().substitute(substitution), positive);
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		Set<VariableTerm> bindingVariables = new HashSet<>();
		if (AggregateLiteral.boundBindingVariable(getAtom().getLowerBoundOperator(), getAtom().getLowerBoundTerm(), positive) != null) {
			bindingVariables.add((VariableTerm) getAtom().getLowerBoundTerm());
		}
		return bindingVariables;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		// Note: every local variable that also occurs globally in the rule is a nonBindingVariable, hence an
		// aggregate literal alone cannot detect its non-binding (i.e. global) variables.
		throw new UnsupportedOperationException();
	}
	
}
