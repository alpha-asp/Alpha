package at.ac.tuwien.kr.alpha.solver;

import java.util.Set;

public interface SimpleAssignment {
	boolean isAssigned(int atom);
	ThriceTruth getTruth(int atom);

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	interface Entry {
		int getAtom();
		ThriceTruth getTruth();
	}
}
