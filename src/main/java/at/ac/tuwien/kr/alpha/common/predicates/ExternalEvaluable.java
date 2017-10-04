package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

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

		// TODO: Check whether all parameters of method are some termy things.

		this.method = method;
	}

	@Override
	public boolean evaluate(List<Term> terms, Substitution substitution) {
		if (terms.size() != getArity()) {
			throw new IllegalArgumentException("Number of terms does not match.");
		}

		Term[] arguments = new Term[terms.size()];

		for (int i = 0; i < arguments.length; i++) {
			Term it = terms.get(i);

			if (it instanceof VariableTerm) {
				it = it.substitute(substitution);
			}

			arguments[i] = it;
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
