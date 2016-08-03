package at.ac.tuwien.kr.alpha.common;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class FunctionTerm implements Term {

	public Integer functionSymbol;
	public List<Term> termList;

	@Override
	public boolean isGround() {
		for (Term term : termList) {
			if (!term.isGround()) {
				return false;
			}
		}
		return true;
	}
}
