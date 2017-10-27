package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class ExternalLongPredicate extends ExternalNonBindingPredicate {
	private final java.util.function.LongPredicate predicate;

	public ExternalLongPredicate(String name, java.util.function.LongPredicate predicate) {
		super(name, 1);
		this.predicate = predicate;
	}

	@Override
	protected boolean test(List<ConstantTerm> terms) {
		if (!(terms.get(0).getObject() instanceof Long)) {
			throw new IllegalArgumentException(name + " expects a long.");
		}
		return predicate.test((Long) terms.get(0).getObject());
	}
}
