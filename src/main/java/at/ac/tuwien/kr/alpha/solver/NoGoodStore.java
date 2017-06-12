package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

/**
 * A (temporary) interface defining the use of a NoGoodStore.
 * Copyright (c) 2016, the Alpha Team.
 */
public interface NoGoodStore {
	/**
	 * Adds a nogood with the given id.
	 * @param id the unique identifier of the nogood.
	 * @param noGood the nogood to add.
	 * @return null if the noGood was added without conflict, a ConflictCause describing the conflict otherwise.
	 */
	ConflictCause add(int id, NoGood noGood);

	/**
	 * Apply unit-propagation and mbt-propagation. Propagation should stop as soon as some NoGood is violated.
	 * @return true iff the propagation derived new assignment(s).
	 */
	ConflictCause propagate();

	boolean hasInferredAssignments();

	void backtrack();
}
