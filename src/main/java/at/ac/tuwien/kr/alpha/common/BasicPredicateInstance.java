package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedAtom;

public class BasicPredicateInstance extends PredicateInstance<BasicPredicate> {
	public BasicPredicateInstance(ParsedAtom parsedAtom) {
		super(new BasicPredicate(parsedAtom.predicate, parsedAtom.arity), terms(parsedAtom));
	}

	private static Term[] terms(ParsedAtom parsedAtom) {
		Term[] terms;
		if (parsedAtom.arity == 0) {
			terms = new Term[0];
		} else {
			terms = new Term[parsedAtom.terms.size()];
			for (int i = 0; i < parsedAtom.terms.size(); i++) {
				terms[i] = AtomStore.convertFromParsedTerm(parsedAtom.terms.get(i));
			}
		}
		return terms;
	}
}
