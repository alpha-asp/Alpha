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
}
