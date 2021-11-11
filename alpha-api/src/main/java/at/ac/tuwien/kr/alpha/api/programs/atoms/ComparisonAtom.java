package at.ac.tuwien.kr.alpha.api.programs.atoms;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;

/**
 * An {@link Atom} representing a comparison of terms.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ComparisonAtom extends Atom, VariableNormalizableAtom {

	ComparisonOperator getOperator();

	@Override
	ComparisonAtom substitute(Substitution subst);

}
