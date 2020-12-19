package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

public interface Atom extends Comparable<Atom> {
	Predicate getPredicate();

	List<? extends Term> getTerms();

	/**
	 * Returns whether this atom is ground, i.e., variable-free.
	 *
	 * @return true iff the terms of this atom contain no {@link VariableTerm}.
	 */
	boolean isGround();

	Literal toLiteral();

	/**
	 * Creates a literal containing this atom which will be negated if {@code positive} is {@code false}.
	 *
	 * @param positive the polarity of the resulting literal.
	 * @return a literal that is positive iff the given parameter is true.
	 */
	Literal toLiteral(boolean positive);
}
