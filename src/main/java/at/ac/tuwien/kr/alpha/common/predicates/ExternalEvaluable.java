package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ExternalEvaluable implements Predicate, Evaluable {
	private final Method method;

	public ExternalEvaluable(Method method)
	{
		if (!method.getReturnType().equals(boolean.class)) {
			throw new IllegalArgumentException("method must return boolean");
		}

		this.method = method;
	}

	@Override
	public boolean evaluate(List<Term> terms, Substitution substitution) {
		if (terms.size() != getArity()) {
			throw new IllegalArgumentException(
				"Parameter count mismatch when calling " + getPredicateName() + ". " +
				"Expected " + getArity() + " parameters but got " + terms.size() + "."
			);
		}

		final Class<?>[] parameterTypes = method.getParameterTypes();

		final Object[] arguments = new Object[terms.size()];

		for (int i = 0; i < arguments.length; i++) {
			Term it = terms.get(i);

			if (it instanceof VariableTerm) {
				it = it.substitute(substitution);
			}

			if (!(it instanceof ConstantTerm)) {
				throw new RuntimeException("Non-constant term as parameter for evaluable. Should not happen.");
			}

			arguments[i] = ((ConstantTerm) it).getObject();

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
			return (boolean) method.invoke(null, arguments);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getPredicateName() {
		return method.getName();
	}

	@Override
	public int getArity() {
		return method.getParameterCount();
	}
}
