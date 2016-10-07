package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * A (temporary) interface defining the use of a NoGoodStore.
 * Copyright (c) 2016, the Alpha Team.
 */
public interface NoGoodStoreInterface {
	/**
	 * Initialize the NoGoodStore.
	 */
	void init();

	/**
	 * Backtracks to the indicated decision level. Every assignment on a higher decisionLevel is removed.
	 * All assignments below (or equal to) decisionLevel are kept. Note that for atoms being TRUE this may require
	 * setting the assigned value to MBT during backtracking.
	 * @param decisionLevel the decision level to backtrack to.
	 */
	void backtrack(int decisionLevel);

	/**
	 * Assigns the given atom the given truth value at the current decision level.
	 * @param atom the atom to assign.
	 * @param value the truth value to assign.
	 */
	void assign(int atom, ThriceTruth value);


	/**
	 * Sets the current decision level to the given one.
	 * @param decisionLevel the decision level.
	 */
	void setDecisionLevel(int decisionLevel);


	/**
	 * Adds a nogood with the given id.
	 * @param noGoodId the unique identifier of the nogood.
	 * @param noGood the nogood to add.
	 */
	void addNoGood(int noGoodId, NoGood noGood);


	/**
	 * Returns whether the current assignment violates some nogood.
	 * @return true iff there exists a nogood that is violated by the current assignment.
	 */
	boolean isNoGoodViolated();


	/**
	 * Apply unit-propagation and mbt-propagation. Propagation should stop as soon as some NoGood is violated.
	 * @return true iff the propagation derived new assignment(s).
	 */
	boolean doPropagation();


	/**
	 * Returns the assignments that have changed since the last call to getChangedAssignments, i.e. it returns all
	 * assignments done by doPropagation() and assign(..)
	 * @return a list of all assignments done by propagation.
	 */
	List<Pair<Integer, ThriceTruth>> getChangedAssignments();


	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	List<Integer> getTrueAssignments();


	/**
	 * Reports if the current assignment is free of must-be-true values.
	 * @return true iff the current assignment contains only TRUE and FALSE as assigned values.
	 */
	boolean isAssignmentMBTFree();


	/**
	 * Returns the truth value assigned to an atom.
	 * @param atomId the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	ThriceTruth getTruthValue(int atomId);
}
