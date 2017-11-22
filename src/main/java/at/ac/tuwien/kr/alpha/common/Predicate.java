package at.ac.tuwien.kr.alpha.common;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class Predicate implements Comparable<Predicate> {
	private static final Interner<Predicate> INTERNER = new Interner<>();

	private final String name;
	private final int arity;
	private final boolean internal;

	protected Predicate(String name, int arity, boolean internal) {
		this.name = name;
		this.arity = arity;
		this.internal = internal;
	}

	public static Predicate getInstance(String symbol, int arity) {
		return getInstance(symbol, arity, false);
	}

	public static Predicate getInstance(String symbol, int arity, boolean internal) {
		return INTERNER.intern(new Predicate(symbol, arity, internal));
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + arity;
		result = 31 * result + (internal ? 1 : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Predicate)) {
			return false;
		}

		Predicate predicate = (Predicate) o;

		if (arity != predicate.arity) {
			return false;
		}

		if (internal != predicate.internal) {
			return false;
		}

		return name != null ? name.equals(predicate.name) : predicate.name == null;
	}

	public boolean isInternal() {
		return internal;
	}

	@Override
	public int compareTo(Predicate other) {
		int result = getName().compareTo(other.getName());

		if (result != 0) {
			return result;
		}

		return Integer.compare(getArity(), other.getArity());
	}

	public String getName() {
		return name;
	}

	public int getArity() {
		return arity;
	}
}
