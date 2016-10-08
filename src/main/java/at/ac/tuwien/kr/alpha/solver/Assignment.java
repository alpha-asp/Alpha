package at.ac.tuwien.kr.alpha.solver;

import java.util.Set;

import static at.ac.tuwien.kr.alpha.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;

public interface Assignment {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	/**
	 * Backtracks to the indicated decision level. Every assignment on a higher decisionLevel is removed.
	 * All assignments below (or equal to) decisionLevel are kept. Note that for atoms being TRUE this may require
	 * setting the assigned value to MBT during backtracking.
	 * @param decisionLevel the decision level to backtrack to.
	 */
	void backtrack(int decisionLevel);

	/**
	 * Reports if the current assignment is free of must-be-true values.
	 * @return false iff the current assignment contains only TRUE and FALSE as assigned values.
	 */
	boolean containsMBT();

	/**
	 * Assigns the given atom the given truth value at the current decision level.
	 * @param atom the atom to assign.
	 * @param value the truth value to assign.
	 */
	void assign(int atom, ThriceTruth value, int decisionLevel);

	/**
	 * Returns the truth value assigned to an atom.
	 * @param atom the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	default ThriceTruth getTruth(int atom) {
		final Entry entry = get(atom);
		if (entry != null) {
			return entry.getTruth();
		}
		return null;
	}

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	Entry get(int atom);

	interface Entry {
		ThriceTruth getTruth();
		int getDecisionLevel();
	}

	default boolean isAssigned(int atom) {
		return get(atom) != null;
	}

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		if (entry == null) {
			return false;
		}
		return isNegated(literal) == (FALSE.equals(getTruth(atomOf(literal))));
	}
}
