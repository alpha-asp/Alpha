package at.ac.tuwien.kr.alpha.api.programs;

/**
 * A (first-order logic) predicate as used in ASP programs accepted by Alpha.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Predicate extends Comparable<Predicate> {

	/**
	 * The predicate symbol.
	 */
	String getName();

	/**
	 * The arity, i.e. number of arguments of an instance, of this predicate.
	 */
	int getArity();

	@Override
	default int compareTo(Predicate other) {
		int result = getName().compareTo(other.getName());

		if (result != 0) {
			return result;
		}

		return Integer.compare(getArity(), other.getArity());
	}

	/**
	 * Indicates whether this predicate is internal. Internal predicates are no shown in answer sets.
	 */
	boolean isInternal();

	/**
	 * Indicates whether this predicate is internal to the solver. Solver-internal predicates are not shown in answer sets and may not be
	 * contained in input programs.
	 */
	boolean isSolverInternal();
}
