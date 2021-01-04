package at.ac.tuwien.kr.alpha.api.program;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;

public interface Atom extends Comparable<Atom> {
	Predicate getPredicate();

	List<Term> getTerms();

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
