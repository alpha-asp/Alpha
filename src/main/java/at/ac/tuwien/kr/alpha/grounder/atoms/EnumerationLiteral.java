package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class EnumerationLiteral extends FixedInterpretationLiteral {

	public EnumerationLiteral(EnumerationAtom atom) {
		super(atom, true);
	}

	@Override
	public List<Substitution> getSubstitutions(Substitution partialSubstitution) {
		return null;
	}

	@Override
	public Literal negate() {
		throw new UnsupportedOperationException("EnumerationLiteral cannot be negated");
	}

	@Override
	public Literal substitute(Substitution substitution) {
		return null;
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		return null;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		return null;
	}
}
