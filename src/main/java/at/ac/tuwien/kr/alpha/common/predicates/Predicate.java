package at.ac.tuwien.kr.alpha.common.predicates;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class Predicate implements Comparable<Predicate> {
	protected final String name;
	protected final int arity;

	public Predicate(String name, int arity) {
		this.name = name;
		this.arity = arity;
	}

	public String getPredicateName() {
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

		if (arity != that.arity) {
			return false;
		}

		return name.equals(that.name);
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
		int result = this.getArity() - other.getArity();

		if (result != 0) {
			return result;
		}

		return getPredicateName().compareTo(other.getPredicateName());
	}
}
