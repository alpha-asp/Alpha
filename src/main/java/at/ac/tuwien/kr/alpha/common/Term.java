package at.ac.tuwien.kr.alpha.common;

import java.util.List;

/**
 * Common representation of Terms. Terms are constructed such that each term is represented by a unique object, hence
 * term equality can be checked by object reference comparison. Each concrete subclass of a Term must implement a
 * factory-like method to obtain instances.
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class Term implements Comparable<Term> {
	public abstract boolean isGround();

	public abstract List<VariableTerm> getOccurringVariables();

	@Override
	public abstract String toString();
}
