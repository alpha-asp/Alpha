package at.ac.tuwien.kr.alpha.api.grounder;

import java.util.TreeMap;

import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;

/**
 * A mapping from {@link VariableTerm}s to {@link Term}s used during grounding to represent ground instances of terms, literals and rules.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Substitution {

	Term eval(VariableTerm variableTerm);

	TreeMap<VariableTerm, Term> getSubstitution();

	<T extends Comparable<T>> Term put(VariableTerm variableTerm, Term groundTerm);

	boolean isVariableSet(VariableTerm var);

}
