package at.ac.tuwien.kr.alpha.solver;

import java.util.Set;

import static at.ac.tuwien.kr.alpha.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.Literals.isNegated;

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
	 * Reports how many atoms are assigned to must-be-true currently. If this method returns
	 * zero, the assignment is guaranteed to be free of must-be-true values (i.e. it only
	 * contains assignments to either true or false).
	 * @return the count of must-be-true values in the asignment.
	 */
	int getMBTCount();

	/**
	 * Assigns the given atom the given truth value at the given decision level.
	 * @param atom the atom to assign.
	 * @param value the truth value to assign.
	 */
	boolean assign(int atom, ThriceTruth value, int decisionLevel);

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

	/**
	 * Returns the truth value assigned to an atom.
	 * @param atom the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	default ThriceTruth getTruth(int atom) {
		final Entry entry = get(atom);
		return entry == null ? null : entry.getTruth();
	}

	default boolean isAssigned(int atom) {
		return get(atom) != null;
	}

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && isNegated(literal) == !entry.getTruth().toBoolean();
	}
}
