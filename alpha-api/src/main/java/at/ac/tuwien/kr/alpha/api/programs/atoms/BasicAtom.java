package at.ac.tuwien.kr.alpha.api.programs.atoms;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;

/**
 * A "normal" atom according to the ASP Core 2 Standard.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface BasicAtom extends Atom, VariableNormalizableAtom {

	@Override
	BasicAtom substitute(Substitution substitution);

}
