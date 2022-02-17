package at.ac.tuwien.kr.alpha.api.programs.atoms;

import java.util.List;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;
import at.ac.tuwien.kr.alpha.api.terms.Term;

/**
 * A "normal" atom according to the ASP Core 2 Standard.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface BasicAtom extends Atom, VariableNormalizableAtom {

	@Override
	BasicAtom substitute(Substitution substitution);

	@Override
	BasicAtom renameVariables(Function<String, String> mapping);

	@Override
	BasicAtom withTerms(List<Term> terms);

}
