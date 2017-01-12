package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

public interface SimpleAssignment<T extends Truth> extends SimpleReadableAssignment<T> {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	boolean assign(int atom, T value);

	void unassign(int atom);
}
