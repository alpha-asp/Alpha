package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.ImmutableAssignment;
import at.ac.tuwien.kr.alpha.common.NoGood;

public interface Assignment extends ImmutableAssignment, Iterable<ImmutableAssignment.Entry> {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	/**
	 * Backtracks to the indicated decision level. Every assignment on a higher decisionLevel is removed.
	 * All assignments below (or equal to) decisionLevel are kept. Note that for atoms being TRUE this may require
	 * setting the assigned value to MBT during backtracking.
	 */
	void backtrack();

	boolean assign(int atom, ThriceTruth value, NoGood impliedBy);

	/**
	 * Assigns an atom some value on a lower decision level than the current one.
	 * @param atom
	 * @param value
	 * @param impliedBy
	 * @param decisionLevel
	 * @return
	 */
	boolean assignSubDL(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel);

	default boolean assign(int atom, ThriceTruth value) {
		return assign(atom, value, null);
	}

	boolean guess(int atom, ThriceTruth value);
}
