package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.Evaluable;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalEvaluable;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.predicates.TotalOrder;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParsedExternalAtom extends ParsedAtom {
	public ParsedExternalAtom(String predicate, List<ParsedTerm> inputTerms, List<ParsedTerm> outputTerms) {
		super(predicate, concat(inputTerms, outputTerms));
	}

	private static List<ParsedTerm> concat(List<ParsedTerm> a, List<ParsedTerm> b) {
		List<ParsedTerm> result = new ArrayList<>(a.size() + b.size());
		result.addAll(a);
		result.addAll(b);
		return result;
	}

	@Override
	public Atom toAtom(Map<String, ExternalEvaluable> externals) {
		Predicate external = externals.get(this.predicate);

		// TODO: This is very fragile. Do a nice fallback for built-in predicates such as TotalOrder.

		if (external == null) {
			external = new TotalOrder(this.predicate, this.isNegated);
		}

		List<Term> terms = new ArrayList<>(this.terms.size());

		for (ParsedTerm parsedTerm : this.terms) {
			terms.add(parsedTerm.toTerm());
		}

		return new BasicAtom(external, terms);
	}
}
