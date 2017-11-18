package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Template for predicate interpretations that do not generate new bindings but only return
 * a truth value.
 */
public abstract class NonBindingPredicateInterpretation implements PredicateInterpretation {
	private final int arity;

	public NonBindingPredicateInterpretation(int arity) {
		this.arity = arity;
	}

	public NonBindingPredicateInterpretation() {
		this(1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<List<ConstantTerm>> evaluate(List<Term> terms) {
		if (terms.size() != arity) {
			throw new IllegalArgumentException("Exactly " + arity + " term(s) required.");
		}

		final List<ConstantTerm> constants = new ArrayList<>(terms.size());
		for (int i = 0; i < terms.size(); i++) {
			if (!(terms.get(i) instanceof ConstantTerm)) {
				throw new IllegalArgumentException(
					"Expected only constants as input, but got " +
						"something else at position " + i + "."
				);
			}

			constants.add((ConstantTerm) terms.get(i));
		}

		try {
			return test(constants) ? TRUE : FALSE;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Argument types do not match.", e);
		}
	}

	protected abstract boolean test(List<ConstantTerm> terms);
}
