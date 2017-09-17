package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BuiltinAtom;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedBuiltinAtom extends ParsedAtom {
	public ParsedBuiltinAtom(String predicate, List<ParsedTerm> terms) {
		super(predicate, terms);
	}

	@Override
	public Atom toAtom() {
		if (this.terms.size() != 2) {
			throw new UnsupportedOperationException("Builtin with not exactly two terms cannot be handled.");
		}

		List<Term> terms = new ArrayList<>(2);
		terms.add(this.terms.get(0).toTerm());
		terms.add(this.terms.get(1).toTerm());
		return new BuiltinAtom(this.predicate, terms, this.isNegated);
	}
}