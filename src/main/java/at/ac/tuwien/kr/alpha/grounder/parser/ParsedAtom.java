package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedAtom extends CommonParsedObject {
	protected final String predicate;
	protected final List<ParsedTerm> terms;
	protected final int arity;
	protected boolean isNegated;

	public ParsedAtom(String predicate, int arity, List<ParsedTerm> terms) {
		this.predicate = predicate;
		this.arity = arity;
		this.terms = terms;
	}

	public ParsedAtom(String predicate, List<ParsedTerm> terms) {
		this.predicate = predicate;
		this.arity = terms.size();
		this.terms = terms;
	}

	public String getPredicate() {
		return predicate;
	}

	public List<ParsedTerm> getTerms() {
		return terms;
	}

	public int getArity() {
		return arity;
	}

	public boolean isNegated() {
		return isNegated;
	}

	@Override
	public String toString() {
		String ret = (isNegated ? " not " : "") + predicate + "(";
		for (int i = 0; i < arity; i++) {
			ret += (i == 0 ? "" : ", ") + terms.get(i);
		}
		ret += ")";
		return ret;
	}
}
