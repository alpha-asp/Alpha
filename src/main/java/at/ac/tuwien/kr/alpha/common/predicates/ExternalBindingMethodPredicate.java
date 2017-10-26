package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class ExternalBindingMethodPredicate extends FixedInterpretationPredicate {
	private final Method method;

	public ExternalBindingMethodPredicate(Method method) {
		super(method.getName(), method.getParameterCount());

		if (!method.getReturnType().equals(Set.class)) {
			throw new IllegalArgumentException("method must return Set");
		}

		this.method = method;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<List<ConstantTerm>> evaluate(List<Term> terms) {
		if (terms.size() != getArity()) {
			throw new IllegalArgumentException(
				"Parameter count mismatch when calling " + getPredicateName() + ". " +
					"Expected " + getArity() + " parameters but got " + terms.size() + "."
			);
		}

		final Class<?>[] parameterTypes = method.getParameterTypes();

		final Object[] arguments = new Object[terms.size()];

		for (int i = 0; i < arguments.length; i++) {
			if (!(terms.get(i) instanceof ConstantTerm)) {
				throw new IllegalArgumentException(
					"Expected only constants as input for " + getPredicateName() + ", but got " +
						"something else at position " + i + "."
				);
			}

			arguments[i] = ((ConstantTerm) terms.get(i)).getObject();

			final Class<?> expected = parameterTypes[i];
			final Class<?> actual = arguments[i].getClass();

			if (expected.isAssignableFrom(actual)) {
				continue;
			}

			if (expected.isPrimitive() && ClassUtils.primitiveToWrapper(expected).isAssignableFrom(actual)) {
				continue;
			}

			throw new IllegalArgumentException(
				"Parameter type mismatch when calling " + getPredicateName() +
					" at position " + i + ". Expected " + expected + " but got " +
					actual + "."
			);
		}

		try {
			return (Set) method.invoke(null, arguments);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
