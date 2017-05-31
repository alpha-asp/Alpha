package at.ac.tuwien.kr.alpha.solver;

public interface SimpleAssignment extends SimpleReadableAssignment {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	boolean assign(int atom, ThriceTruth value);

	void unassign(int atom);
}
