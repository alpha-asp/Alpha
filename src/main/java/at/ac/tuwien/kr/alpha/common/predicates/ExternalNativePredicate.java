package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class ExternalNativePredicate<T> extends BaseExternalNativePredicate {
	private final java.util.function.Predicate<T> predicate;

	public ExternalNativePredicate(String name, java.util.function.Predicate<T> predicate) {
		super(name, 1);
		this.predicate = predicate;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean test(List<ConstantTerm> terms) {
		return predicate.test((T) terms.get(0).getObject());
	}
}
