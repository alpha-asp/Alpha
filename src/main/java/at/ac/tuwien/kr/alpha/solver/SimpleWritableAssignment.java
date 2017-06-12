package at.ac.tuwien.kr.alpha.solver;

public interface SimpleWritableAssignment extends SimpleAssignment {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	ConflictCause assign(int atom, ThriceTruth value);

	void unassign(int atom);

	/**
	 * Backtracks to the indicated decision level. Every assignment on a higher decisionLevel is removed.
	 * All assignments below (or equal to) decisionLevel are kept. Note that for atoms being TRUE this may require
	 * setting the assigned value to MBT during backtracking.
	 */
	void backtrack();

	ConflictCause guess(int atom, ThriceTruth value);

	default ConflictCause guess(int atom, boolean value) {
		return guess(atom, ThriceTruth.valueOf(value));
	}
}
