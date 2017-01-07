package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedAtom extends CommonParsedObject{
	public final String predicate;
	public final List<ParsedTerm> terms;

	public boolean isNegated;

	public ParsedAtom(String predicate, List<ParsedTerm> terms) {
		this.predicate = predicate;
		this.terms = terms;
	}

	public int getArity() {
		return terms.size();
	}

	@Override
	public String toString() {
		String ret = (isNegated ? " not " : "") + predicate + "(";
		for (int i = 0; i < terms.size(); i++) {
			ret += (i == 0 ? "" : ", ") + terms.get(i);
		}
		ret += ")";
		return ret;
	}
}
