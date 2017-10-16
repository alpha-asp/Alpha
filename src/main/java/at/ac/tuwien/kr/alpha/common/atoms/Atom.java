package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
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

	/**
	 * List of all variables occurring in the Atom that are potentially binding, i.e., variables in positive atoms.
	 * @return
	 */
	List<VariableTerm> getBindingVariables();

	/**
	 * List of all variables occurring in the Atom that are never binding, not even in positive atoms, e.g., variables in intervals or built-in atoms.
	 * @return
	 */
	List<VariableTerm> getNonBindingVariables();

	/**
	 * This method applies a substitution to a potentially non-substitute atom.
	 * The resulting atom may be non-substitute.
	 * @param substitution the variable substitution to apply.
	 * @return the atom resulting from the applying the substitution.
	 */
	Atom substitute(Substitution substitution);
}
