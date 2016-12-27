package at.ac.tuwien.kr.alpha.common;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public interface Atom {
	Predicate getPredicate();
	Term[] getTerms();

	boolean isGround();
	boolean isInternal();

	List<VariableTerm> getOccurringVariables();
}
