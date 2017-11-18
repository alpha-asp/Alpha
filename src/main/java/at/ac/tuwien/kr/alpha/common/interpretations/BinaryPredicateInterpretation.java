package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class BinaryPredicateInterpretation<T, U> extends NonBindingPredicateInterpretation {
	private final java.util.function.BiPredicate<T, U> predicate;

	public BinaryPredicateInterpretation(java.util.function.BiPredicate<T, U> predicate) {
		super(2);
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
