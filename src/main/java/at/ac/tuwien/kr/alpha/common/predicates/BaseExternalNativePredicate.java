package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

public abstract class BaseExternalNativePredicate implements Predicate, FixedEvaluable {
	protected final String name;
	protected final int arity;

	public BaseExternalNativePredicate(String name, int arity) {
		this.name = name;
		this.arity = arity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean evaluate(List<ConstantTerm> terms) {
		if (terms.size() != arity) {
			throw new IllegalArgumentException(name + " can only be used to test exactly " + arity + " term(s).");
		}

		try {
			return test(terms);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Argument types for " + name + " do not match.", e);
		}
	}

	protected abstract boolean test(List<ConstantTerm> terms);

	@Override
	public String getPredicateName() {
		return name;
	}

	@Override
	public int getArity() {
		return 1;
	}
}
