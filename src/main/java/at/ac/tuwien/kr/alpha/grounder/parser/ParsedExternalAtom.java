package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.TotalOrder;

import java.util.ArrayList;
import java.util.List;

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
	public Atom toAtom() {
		return new BasicAtom(new TotalOrder(this.predicate, this.isNegated), this.terms.get(0).toTerm(), this.terms.get(1).toTerm());
	}
}
