package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.common.Interner;
import at.ac.tuwien.kr.alpha.common.Symbol;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm<T extends Comparable<T>> extends Term {
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

	/**
	 * Establishes "priority" for ordering of constant terms depending on the type
	 * of the corresponding object according to ASP-Core-2.03c.
	 */
	private static final int priority(final Class<?> clazz) {
		if (clazz.equals(Integer.class)) {
			return 1;
		} else if (clazz.equals(Symbol.class)) {
			return 2;
		} else if (clazz.equals(String.class)) {
			return 3;
		}
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Term o) {
		if (this == o) {
			return 0;
		}

		if (!(o instanceof ConstantTerm)) {
			return super.compareTo(o);
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

		Class<?> thisType = this.object.getClass();
		Class<?> otherType = other.object.getClass();

		int thisPrio = priority(thisType);
		int otherPrio = priority(otherType);

		if (thisPrio == 0 || otherPrio == 0) {
			return thisType.getName().compareTo(otherType.getName());
		}

		return Integer.compare(thisPrio, otherPrio);
	}
}
