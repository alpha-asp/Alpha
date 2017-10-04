package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalEvaluable;
import at.ac.tuwien.kr.alpha.common.predicates.TotalOrder;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedBuiltinAtom extends ParsedAtom {
	public ParsedBuiltinAtom(String predicate, List<ParsedTerm> terms) {
		super(predicate, terms);
	}

	@Override
	public Atom toAtom(Map<String, ExternalEvaluable> externals) {
		if (this.terms.size() != 2) {
			throw new UnsupportedOperationException("Builtin with not exactly two terms cannot be handled.");
		}

		return new BasicAtom(new TotalOrder(this.predicate, this.isNegated), this.terms.get(0).toTerm(), this.terms.get(1).toTerm());
	}
}