package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class ExternalBiPredicate<T, U> extends BaseExternalPredicate {
	private final java.util.function.BiPredicate<T, U> predicate;

	public ExternalBiPredicate(String name, java.util.function.BiPredicate<T, U> predicate) {
		super(name, 2);
		this.predicate = predicate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean test(List<ConstantTerm> terms) {
		return predicate.test(
			(T) terms.get(0).getObject(),
			(U) terms.get(1).getObject()
		);
	}
}
