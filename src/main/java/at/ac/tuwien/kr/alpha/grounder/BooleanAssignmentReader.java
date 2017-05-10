package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.solver.Assignment;

/**
 * Helper class to allow the grounder to check whether an atom is currently assigned true, in order to exclude some
 * unnecessary ground instances.
 * Reporting the current assignment needs not be perfect but report an atom as true whenever it is indeed true.
 * Copyright (c) 2017, the Alpha Team.
 */
public class BooleanAssignmentReader {
	private Assignment assignment;
	public BooleanAssignmentReader(Assignment assignment) {
		this.assignment = assignment;
	}

	/**
	 * Returns true if the given atom is true. It may return true even if it is false.
	 * @param atomId the atom
	 * @return true if the given atom is true and true or false otherwise.
	 */
	public boolean isTrue(int atomId) {
		if (assignment == null) {
			return true;
		}
		Assignment.Entry entry = assignment.get(atomId);
		return entry != null && entry.getTruth().toBoolean();
	}
}
