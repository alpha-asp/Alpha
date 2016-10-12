package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.Map;

/**
 * A (temporary) interface defining the use of a NoGoodStore.
 * Copyright (c) 2016, the Alpha Team.
 */
public interface NoGoodStore<T extends ThriceTruth> {
	/**
	 * Sets the current decision level to the given one.
	 * @param decisionLevel the decision level.
	 */
	void setDecisionLevel(int decisionLevel);

	/**
	 * Assigns the given atom the given truth value at the current decision level.
	 * @param atom the atom to assign.
	 * @param value the truth value to assign.
	 */
	boolean assign(int atom, T value);

	/**
	 * Adds a nogood with the given id.
	 * @param id the unique identifier of the nogood.
	 * @param noGood the nogood to add.
	 */
	boolean add(int id, NoGood noGood);

	default boolean addAll(Map<Integer, ? extends NoGood> m) {
		for (Map.Entry<Integer, ? extends NoGood> e : m.entrySet()) {
			if (!add(e.getKey(), e.getValue())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether a set of nogoods that are violated by the current assignment.
	 * @return the set of violated nogoods.
	 */
	NoGood getViolatedNoGood();

	/**
	 * Apply unit-propagation and mbt-propagation. Propagation should stop as soon as some NoGood is violated.
	 * @return true iff the propagation derived new assignment(s).
	 */
	boolean propagate();

	/**
	 * Returns the assignments that have changed since the last call to getChangedAssignments, i.e. it returns all
	 * assignments done by doPropagation() and assign(..)
	 * @return a map of all assignments done by propagation.
	 */
	Map<Integer, ThriceTruth> getChangedAssignments();
}
