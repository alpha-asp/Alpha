package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public class IntPredicateInterpretation extends NonBindingPredicateInterpretation {
	private final java.util.function.IntPredicate predicate;

	public IntPredicateInterpretation(java.util.function.IntPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	protected boolean test(List<ConstantTerm> terms) {
		if (!(terms.get(0).getObject() instanceof Integer)) {
			throw new IllegalArgumentException("Integer expected");
		}
		return predicate.test((Integer) terms.get(0).getObject());
	}
}
