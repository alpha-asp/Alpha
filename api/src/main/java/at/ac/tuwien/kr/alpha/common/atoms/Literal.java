package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

public interface Literal {
	Atom getAtom();

	boolean isNegated();

	Literal negate();

	Predicate getPredicate();

	List<? extends Term> getTerms();

	boolean isGround();
}
