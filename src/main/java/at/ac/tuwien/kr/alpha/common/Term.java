package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

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

	/**
	 * Applies a substitution, result may be nonground.
	 * @param substitution the variable substitution to apply.
	 * @return the non-substitute term where all variable substitutions have been applied.
	 */
	public abstract Term substitute(Substitution substitution);

	@Override
	public abstract String toString();
}
