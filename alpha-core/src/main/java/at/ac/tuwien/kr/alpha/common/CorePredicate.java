package at.ac.tuwien.kr.alpha.common;

/**
 * A predicate as used by the Alpha solver internally.
 * 
 * Copyright (c) 2016-2020, the Alpha Team.
 */
public class CorePredicate implements Comparable<CorePredicate> {
	
	private static final Interner<CorePredicate> INTERNER = new Interner<>();

	private final String name;
	private final int arity;
	private final boolean internal;
	private final boolean solverInternal;

	protected CorePredicate(String name, int arity, boolean internal, boolean solverInternal) {
		this.name = name;
		this.arity = arity;
		this.internal = internal;
		this.solverInternal = solverInternal;
	}

	public static CorePredicate getInstance(String symbol, int arity) {
		return getInstance(symbol, arity, false, false);
	}

	public static CorePredicate getInstance(String symbol, int arity, boolean internal) {
		return getInstance(symbol, arity, internal, false);
	}

	public static CorePredicate getInstance(String symbol, int arity, boolean internal, boolean solverInternal) {
		return INTERNER.intern(new CorePredicate(symbol, arity, internal, solverInternal));
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

		if (!(o instanceof CorePredicate)) {
			return false;
		}

		CorePredicate predicate = (CorePredicate) o;

		if (arity != predicate.arity) {
			return false;
		}

		if (internal != predicate.internal) {
			return false;
		}

		return name != null ? name.equals(predicate.name) : predicate.name == null;
	}

	/**
	 * Marks internal predicates that should not be shown/printed in answer sets.
	 * @return true iff this Predicate should be omitted from answer sets.
	 */
	public boolean isInternal() {
		return internal;
	}

	/**
	 * Marks predicates that are used purely for encoding rules by NoGoods in the solver component. Solver internal
	 * predicates are guaranteed to not occur in any rule bodies and hence are ignored by the grounder.
	 * @return true iff this Predicate is internal to the solver component.
	 */
	public boolean isSolverInternal() {
		return solverInternal;
	}

	@Override
	public int compareTo(CorePredicate other) {
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

	@Override
	public String toString() {
		return name + "/" + arity;
	}
}
