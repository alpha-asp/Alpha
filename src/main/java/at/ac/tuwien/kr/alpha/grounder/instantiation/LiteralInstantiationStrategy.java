package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

/**
 * A {@link LiteralInstantiationStrategy} finds and validates
 * {@link Substitution}s for {@link Literal}s based on a specific definition of
 * when a {@link Substitution} is valid, i.e. what makes a literal "true",
 * "false" or "unassigned".
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public interface LiteralInstantiationStrategy {

	/**
	 * Computes the {@link AssignmentStatus} for the given {@link Literal} according
	 * to the rules of this {@link InstantiationStrategy}.
	 * 
	 * @param groundLiteral a ground {@link Literal} for which to compute an
	 *                      {@link AssignmentStatus}
	 * @return the current {@link AssignmentStatus} for the given literal according
	 *         to the rules of this {@link InstantiationStrategy}
	 */
	AssignmentStatus getTruthForGroundLiteral(Literal groundLiteral);

	/**
	 * Computes {@link Substitution}s that yield ground instances for a given
	 * literal and starting substitution along with the {@link AssignmentStatus} of
	 * respective ground instances. Note that in all implementations it must hold
	 * that an {@link AssignmentStatus} AS for a {@link Substitution} S as yielded
	 * by this method for a {@link Literal} lit is the same as the result of calling
	 * <code>getTruthForGroundLiteral(lit.substitute(S))</code>, i.e. both methods
	 * must yield the same assignment status for the same ground literal.
	 * 
	 * @param lit                 a non-ground {@link Literal} for which to compute
	 *                            substitutions.
	 * @param partialSubstitution a (possibly empty) substitution to use as a
	 *                            starting point
	 * @return a list of substitutions along with the assignment status of the
	 *         respective ground atoms
	 */
	List<ImmutablePair<Substitution, AssignmentStatus>> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution);

}
