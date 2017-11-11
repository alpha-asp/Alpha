package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.Constant;

import java.util.List;

public class ExternalIntPredicate extends ExternalNonBindingPredicate {
	private final java.util.function.IntPredicate predicate;

	public ExternalIntPredicate(java.util.function.IntPredicate predicate) {
		this.predicate = predicate;
	}

	@Override
	protected boolean test(List<Constant> terms) {
		if (!(terms.get(0).getSymbol() instanceof Integer)) {
			throw new IllegalArgumentException("Integer expected");
		}
		return predicate.test((Integer) terms.get(0).getSymbol());
	}
}
