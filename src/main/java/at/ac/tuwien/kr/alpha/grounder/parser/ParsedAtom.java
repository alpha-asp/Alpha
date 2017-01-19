package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.Atom;
import at.ac.tuwien.kr.alpha.common.BasicAtom;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedAtom extends CommonParsedObject {
	protected final String predicate;
	protected final List<ParsedTerm> terms;
	protected boolean isNegated;

	public ParsedAtom(String predicate, List<ParsedTerm> terms) {
		this.predicate = predicate;
		this.terms = terms;
	}

	public String getPredicate() {
		return predicate;
	}

	public List<ParsedTerm> getTerms() {
		return terms;
	}

	public int getArity() {
		return terms.size();
	}

	public boolean isNegated() {
		return isNegated;
	}

	public Atom toAtom() {
		return new BasicAtom(new BasicPredicate(predicate, terms.size()), terms());
	}

	private List<Term> terms() {
		return terms.stream().map(ParsedTerm::toTerm).collect(Collectors.toList());
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