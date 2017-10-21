package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

public class ExternalNativeIntPredicate extends BaseExternalNativePredicate {
	private final java.util.function.IntPredicate predicate;

	public ExternalNativeIntPredicate(String name, java.util.function.IntPredicate predicate) {
		super(name, 1);
		this.predicate = predicate;
	}

	@Override
	protected boolean test(List<ConstantTerm> terms) {
		if (!(terms.get(0).getObject() instanceof Integer)) {
			throw new IllegalArgumentException(name + " expects an integer.");
		}
		return predicate.test((Integer) terms.get(0).getObject());
	}
}
