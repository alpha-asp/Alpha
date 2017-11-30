package at.ac.tuwien.kr.alpha.common.fixedinterpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MethodPredicateInterpretation extends NonBindingPredicateInterpretation {
	private final Method method;

	public MethodPredicateInterpretation(Method method) {
		super(method.getParameterCount());

		if (!method.getReturnType().equals(boolean.class)) {
			throw new IllegalArgumentException("method must return boolean");
		}

		this.method = method;
	}

	@Override
	protected boolean test(List<ConstantTerm> terms) {
		final Class<?>[] parameterTypes = method.getParameterTypes();
		final Object[] arguments = new Object[terms.size()];

		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = terms.get(i).getObject();

			final Class<?> expected = parameterTypes[i];
			final Class<?> actual = arguments[i].getClass();

			if (expected.isAssignableFrom(actual)) {
				continue;
			}

			if (expected.isPrimitive() && ClassUtils.primitiveToWrapper(expected).isAssignableFrom(actual)) {
				continue;
			}

			throw new IllegalArgumentException(
				"Parameter type mismatch at position " + i + ". Expected " + expected + " but got " +
					actual + "."
			);
		}

		try {
			return (boolean) method.invoke(null, arguments);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
