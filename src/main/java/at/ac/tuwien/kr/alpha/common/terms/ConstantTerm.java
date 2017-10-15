package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.grounder.Substitution;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm<T extends Comparable<T>> implements Term {
	private static final Interner<ConstantTerm> INTERNER = Interners.newStrongInterner();

	public T getObject() {
		return object;
	}

	private final T object;

	private ConstantTerm(T object) {
		this.object = object;
	}

	public static <T extends Comparable<T>> ConstantTerm getInstance(T object) {
		return INTERNER.intern(new ConstantTerm<>(object));
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		return Collections.emptyList();
	}

	@Override
	public Term substitute(Substitution substitution) {
		return this;
	}

	@Override
	public String toString() {
		return object.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ConstantTerm that = (ConstantTerm) o;

		return object.equals(that.object);
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	private static final Map<Class<?>, Integer> PRIORITY = new HashMap<>();

	static {
		PRIORITY.put(Integer.class, 0);
		PRIORITY.put(String.class, 1);
	}

	@Override
	public int compareTo(Term o) {
		if (o instanceof ConstantTerm) {
			ConstantTerm otherConstantTerm = (ConstantTerm) o;

			if (otherConstantTerm.object.getClass() == this.object.getClass()) {
				return this.object.compareTo((T) otherConstantTerm.object);
			}

			int myPrio = PRIORITY.getOrDefault(this.object.getClass(), Integer.MAX_VALUE);
			int otherPrio = PRIORITY.getOrDefault(otherConstantTerm.object.getClass(), Integer.MAX_VALUE);

			if (myPrio == otherPrio) {
				throw new RuntimeException("WUT");
			}

			return myPrio - otherPrio;
		}
		if (o instanceof FunctionTerm) {
			return -1;
		}
		if (o instanceof VariableTerm) {
			return -1;
		}
		throw new UnsupportedOperationException("Comparison of terms is not fully implemented.");
	}
}
