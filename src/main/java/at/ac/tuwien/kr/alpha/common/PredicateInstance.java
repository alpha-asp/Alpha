package at.ac.tuwien.kr.alpha.common;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class PredicateInstance {

	public Predicate predicate;
	public List<Term> termList;

	public boolean isGround() {
		for (Term term : termList) {
			if (!term.isGround()) {
				return false;
			}
		}
		return true;
	}
}
