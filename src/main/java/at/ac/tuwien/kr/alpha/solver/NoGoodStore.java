package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

/**
 * An interface defining the use of a nogood store.
 *
 * Copyright (c) 2016, the Alpha Team.
 */
public interface NoGoodStore {
	/**
	 * Adds a nogood with the given id.
	 * @param id the unique identifier of the nogood.
	 * @param noGood the nogood to add.
	 * @return {@code null} if the noGood was added without conflict, a {@link ConflictCause} describing
	 *         the conflict otherwise.
	 */
	ConflictCause add(int id, NoGood noGood);

	/**
	 * Apply weak propagation and strong propagation. Propagation should stop as soon as some nogood is violated.
	 * @return some cause iff a conflict was reached or {@code null} otherwise
	 */
	ConflictCause propagate();

	/**
	 * After a call to {@link #propagate()} this method provides
	 * whether propagation was successful, i.e. at least one new
	 * assignment was inferred.
	 * @return {@code true} iff the last call to {@link #propagate()}
	 *         inferred at least one assignment, {@code false} otherwise.
	 */
	boolean didPropagate();

	void backtrack();

	void growForMaxAtomId(int maxAtomId);
	
	NoGoodCounter getNoGoodCounter();
}
