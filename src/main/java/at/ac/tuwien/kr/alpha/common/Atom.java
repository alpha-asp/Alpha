package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public interface Atom extends Comparable<Atom> {
	Predicate getPredicate();
	List<Term> getTerms();

	boolean isGround();
	boolean isInternal();

	List<VariableTerm> getOccurringVariables();

	/**
	 * This method applies a substitution to a potentially non-substitute atom.
	 * The resulting atom may be non-substitute.
	 * @param substitution the variable substitution to apply.
	 * @return the atom resulting from the applying the substitution.
	 */
	Atom substitute(Substitution substitution);
}