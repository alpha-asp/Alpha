package at.ac.tuwien.kr.alpha.api.programs.atoms;

import java.util.List;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.VariableNormalizableAtom;
import at.ac.tuwien.kr.alpha.api.terms.Term;

/**
 * An {@link Atom} representing a comparison of terms.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ComparisonAtom extends Atom, VariableNormalizableAtom {

	ComparisonOperator getOperator();

	@Override
	ComparisonAtom substitute(Substitution subst);

	@Override
	ComparisonAtom renameVariables(Function<String, String> mapping);

	@Override
	ComparisonAtom withTerms(List<Term> newTerms);

}
