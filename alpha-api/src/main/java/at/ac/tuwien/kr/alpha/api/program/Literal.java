package at.ac.tuwien.kr.alpha.api.program;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.terms.Term;

public interface Literal {
	Atom getAtom();

	boolean isNegated();

	Literal negate();

	Predicate getPredicate();

	List<? extends Term> getTerms();

	boolean isGround();
}
