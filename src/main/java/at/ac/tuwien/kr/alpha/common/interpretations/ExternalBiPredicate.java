package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class ExternalBiPredicate<T, U> extends ExternalNonBindingPredicate {
	private final java.util.function.BiPredicate<T, U> predicate;

	public ExternalBiPredicate(java.util.function.BiPredicate<T, U> predicate) {
		super(2);
		this.predicate = predicate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean test(List<ConstantTerm> terms) {
		return predicate.test(
			(T) terms.get(0).getSymbol(),
			(U) terms.get(1).getSymbol()
		);
	}
}
