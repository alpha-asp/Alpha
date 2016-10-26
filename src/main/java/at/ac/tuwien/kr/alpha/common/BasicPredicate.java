package at.ac.tuwien.kr.alpha.common;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicPredicate implements Predicate {
	private final String name;
	private final int arity;

	public BasicPredicate(String name, int arity) {
		this.name = name;
		this.arity = arity;
	}

	@Override
	public String getPredicateName() {
		return name;
	}

	@Override
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

		BasicPredicate that = (BasicPredicate) o;

		if (arity != that.arity) {
			return false;
		}

		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + arity;
	}
}
