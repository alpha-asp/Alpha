package at.ac.tuwien.kr.alpha.common.predicates;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public interface Predicate extends Comparable<Predicate> {
	String getPredicateName();
	int getArity();

	default int compareTo(Predicate other) {
		int result = this.getArity() - other.getArity();

		if (result != 0) {
			return result;
		}

		return getPredicateName().compareTo(other.getPredicateName());
	}
}
