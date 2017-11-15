package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class ExternalLongPredicate extends ExternalNonBindingPredicate {
	private final java.util.function.LongPredicate predicate;

	public ExternalLongPredicate(java.util.function.LongPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	protected boolean test(List<ConstantTerm> terms) {
		if (!(terms.get(0).getSymbol() instanceof Long)) {
			throw new IllegalArgumentException("Long expected");
		}
		return predicate.test((Long) terms.get(0).getSymbol());
	}
}
