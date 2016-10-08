package at.ac.tuwien.kr.alpha.common;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicPredicate implements Predicate {
	private final String predicateName;
	private final int arity;

	public BasicPredicate(String predicateName, int arity) {
		this.predicateName = predicateName;
		this.arity = arity;
	}

	@Override
	public String getPredicateName() {
		return predicateName;
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
		return predicateName.equals(that.predicateName);
	}

	@Override
	public int hashCode() {
		int result = predicateName.hashCode();
		result = 31 * result + arity;
		return result;
	}
}
