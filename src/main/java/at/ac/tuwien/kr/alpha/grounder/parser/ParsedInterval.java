package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ParsedInterval extends ParsedTerm {
	private final ParsedTerm lowerBound;
	private final ParsedTerm upperBound;

	public ParsedInterval(ParsedTerm lowerBound, ParsedTerm upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public Term toTerm() {
		return IntervalTerm.getInstance(lowerBound.toTerm(), upperBound.toTerm());
	}

	@Override
	public String toString() {
		return lowerBound + ".." + upperBound;
	}
}
