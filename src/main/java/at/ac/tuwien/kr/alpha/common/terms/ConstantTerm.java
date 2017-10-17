package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.common.Interner;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm<T extends Comparable<T>> implements Term {
	private static final Interner<ConstantTerm> INTERNER = new Interner<>();

	public T getObject() {
		return object;
	}

	private final T object;

	private ConstantTerm(T object) {
		this.object = object;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> ConstantTerm<T> getInstance(T object) {
		return (ConstantTerm<T>) INTERNER.intern(new ConstantTerm<>(object));
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
	public ConstantTerm<T> substitute(Substitution substitution) {
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
	@SuppressWarnings("unchecked")
	public int compareTo(Term o) {
		if (o instanceof FunctionTerm) {
			return -1;
		}
		if (o instanceof VariableTerm) {
			return -1;
		}

		if (!(o instanceof ConstantTerm)) {
			throw new UnsupportedOperationException("Comparison of terms is not fully implemented.");
		}

		ConstantTerm other = (ConstantTerm) o;

		// We will perform an unchecked cast.
		// Because of type erasure, we cannot know the exact type
		// of other.object.
		// However, may assume that other.object actually is of
		// type T.
		// We know that this.object if of type T and implements
		// Comparable<T>. We ensure that the class of other.object
		// equals the class of this.object, which in turn is T.
		// That assumption should be quite safe. It can only be
		// wrong if we have some bug that generates strange
		// ConstantTerms at runtime, bypassing the check for T
		// at compile-time.
		if (other.object.getClass() == this.object.getClass()) {
			return this.object.compareTo((T) other.object);
		}

		int myPrio = PRIORITY.getOrDefault(this.object.getClass(), Integer.MAX_VALUE);
		int otherPrio = PRIORITY.getOrDefault(other.object.getClass(), Integer.MAX_VALUE);

		if (myPrio == otherPrio) {
			throw new RuntimeException("WUT");
		}

		return myPrio - otherPrio;
	}
}
