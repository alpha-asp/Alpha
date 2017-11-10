package at.ac.tuwien.kr.alpha.common.predicates;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class Predicate implements Comparable<Predicate> {
	protected final String name;
	protected final int arity;
	private final boolean internal;

	public Predicate(String name, int arity, boolean internal) {
		this.name = name;
		this.arity = arity;
		this.internal = internal;
	}

	public Predicate(String name, int arity) {
		this(name, arity, false);
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Predicate that = (Predicate) o;
		return arity == that.arity && name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + arity;
	}

	@Override
	public String toString() {
		return name + "/" + arity;
	}

	@Override
	public int compareTo(Predicate other) {
		int result = Integer.compare(this.getArity(), other.getArity());

		if (result != 0) {
			return result;
		}

		return getName().compareTo(other.getName());
	}

	public boolean isInternal() {
		return internal;
	}
}
