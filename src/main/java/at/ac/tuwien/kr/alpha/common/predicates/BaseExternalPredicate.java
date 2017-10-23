package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseExternalPredicate extends FixedInterpretationPredicate {
	public BaseExternalPredicate(String name, int arity) {
		super(name, arity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<List<ConstantTerm>> evaluate(List<Term> terms) {
		if (terms.size() != arity) {
			throw new IllegalArgumentException(name + " can only be used to test exactly " + arity + " term(s).");
		}

		final List<ConstantTerm> constants = new ArrayList<>(terms.size());
		for (int i = 0; i < terms.size(); i++) {
			if (!(terms.get(i) instanceof ConstantTerm)) {
				throw new IllegalArgumentException(
					"Expected only constants as input for " + getPredicateName() + ", but got " +
						"something else at position " + i + "."
				);
			}

			constants.add((ConstantTerm) terms.get(i));
		}

		try {
			return test(constants) ? TRUE : FALSE;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Argument types for " + name + " do not match.", e);
		}
	}

	protected abstract boolean test(List<ConstantTerm> terms);
}
