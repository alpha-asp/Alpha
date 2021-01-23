package at.ac.tuwien.kr.alpha.core.atoms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.VariableTermImpl;
import at.ac.tuwien.kr.alpha.core.grounder.Substitution;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class EnumerationLiteral extends BasicLiteral {

	public EnumerationLiteral(EnumerationAtom atom) {
		super(atom, true);
	}

	@Override
	public EnumerationAtom getAtom() {
		return (EnumerationAtom) super.getAtom();
	}

	@Override
	public EnumerationLiteral negate() {
		throw new UnsupportedOperationException("EnumerationLiteral cannot be negated");
	}

	@Override
	public EnumerationLiteral substitute(Substitution substitution) {
		return new EnumerationLiteral(getAtom().substitute(substitution));
	}

	@Override
	public Set<VariableTermImpl> getBindingVariables() {
		return Collections.singleton((VariableTermImpl)getTerms().get(2));
	}

	@Override
	public Set<VariableTermImpl> getNonBindingVariables() {
		Set<VariableTermImpl> ret = new HashSet<>(2);
		CoreTerm idTerm = getTerms().get(0);
		CoreTerm enumTerm = getTerms().get(1);
		if (idTerm instanceof VariableTermImpl) {
			ret.add((VariableTermImpl) idTerm);
		}
		if (enumTerm instanceof VariableTermImpl) {
			ret.add((VariableTermImpl) enumTerm);
		}
		return ret;

	}
	
	public Substitution addEnumerationIndexToSubstitution(Substitution partialSubstitution) {
		return this.getAtom().addEnumerationIndexToSubstitution(partialSubstitution);
	}
}
