package at.ac.tuwien.kr.alpha.solver;

public interface SimpleWritableAssignment extends SimpleAssignment {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	boolean assign(int atom, ThriceTruth value);

	void unassign(int atom);
}
