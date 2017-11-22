package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class UnaryPredicateInterpretation<T> extends NonBindingPredicateInterpretation {
	private final java.util.function.Predicate<T> predicate;

	public UnaryPredicateInterpretation(java.util.function.Predicate<T> predicate) {
		this.predicate = predicate;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean test(List<ConstantTerm> terms) {
		return predicate.test((T) terms.get(0).getObject());
	}
}
