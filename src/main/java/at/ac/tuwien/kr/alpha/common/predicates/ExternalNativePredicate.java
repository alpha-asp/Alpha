package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

public class ExternalNativePredicate implements Predicate, Evaluable {
	private final String name;
	private final java.util.function.Predicate<ConstantTerm> predicate;

	public ExternalNativePredicate(String name, java.util.function.Predicate<ConstantTerm> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public boolean evaluate(List<Term> terms, Substitution substitution) {
		if (terms.size() != 1) {
			throw new IllegalArgumentException("can only test one term");
		}

		return predicate.test((ConstantTerm) terms.get(0).substitute(substitution));
	}

	@Override
	public String getPredicateName() {
		return name;
	}

	@Override
	public int getArity() {
		return 1;
	}
}
