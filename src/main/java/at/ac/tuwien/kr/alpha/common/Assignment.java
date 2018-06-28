package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.Iterator;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.*;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public interface Assignment {
	Entry get(int atom);

	/**
	 * Returns the truth value assigned to an atom.
	 * @param atom the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	default ThriceTruth getTruth(int atom) {
		final Entry entry = get(atom);
		return entry == null ? null : entry.getTruth();
	}

	/**
	 * Returns the weak decision level of the atom if it is assigned.
	 * @param atom the atom.
	 * @return the weak decision level of the atom if it is assigned; otherwise any value may be returned.
	 */
	int getWeakDecisionLevel(int atom);

	/**
	 * Returns the strong decision level of the atom.
	 * @param atom the atom.
	 * @return the strong decision level of the atom or -1.
	 */
	int getStrongDecisionLevel(int atom);

	default boolean isAssigned(int atom) {
		return get(atom) != null;
	}

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && (isNegated(literal) ? FALSE : TRUE).equals(entry.getTruth());
	}

	/**
	 * Determines if the given {@code noGood} is violated in the current assignment.
	 * @param noGood
	 * @return {@code true} iff all literals in {@code noGood} evaluate to true in the current assignment.
	 */
	default boolean isViolated(NoGood noGood) {
		for (Integer literal : noGood) {
			if (!contains(literal)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determines if the given {@code noGood} is undefined in the current assignment.
	 *
	 * @param noGood
	 * @return {@code true} iff at least one literal in {@code noGood} is unassigned.
	 */
	default boolean isUndefined(NoGood noGood) {
		for (Integer literal : noGood) {
			if (!isAssigned(atomOf(literal))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an iterator over all new assignments. New assignments are only returned once.
	 * getNewPositiveAssignmentsIterator and getNewAssignmentsIterator2 are independent of each other (i.e., each has its own backing collection).
	 * @return
	 */
	Iterator<Integer> getNewPositiveAssignmentsIterator();

	Pollable getAssignmentsToProcess();

	/**
	 * Returns the weak decision level of the atom considering also out-of-order assignments.
	 * @param atom the atom.
	 * @return the weakDecisionLevel of the atom if it is not an out-of-order assignment, otherwise the lowest
	 * decision level at which it will be re-assigned.
	 */
	int getRealWeakDecisionLevel(int atom);

	interface Pollable {
		int peek();
		int remove();
		boolean isEmpty();
	}

	interface Entry {
		ThriceTruth getTruth();
		int getAtom();
		int getDecisionLevel();
		NoGood getImpliedBy();
		int getPropagationLevel();

		boolean hasPreviousMBT();
		int getMBTDecisionLevel();
		int getMBTPropagationLevel();
		NoGood getMBTImpliedBy();

		default int getPropagationLevelRespectingLowerMBT() {
			return hasPreviousMBT() ? getMBTPropagationLevel() : getPropagationLevel();
		}

		default NoGood getImpliedByRespectingLowerMBT() {
			if (hasPreviousMBT()) {
				return getMBTImpliedBy();
			}
			return getImpliedBy();
		}

		/**
		 * Returns the literal corresponding to this assignment
		 * @return atomId if this entry is TRUE/MBT and -atomId if entry is FALSE.
		 */
		default int getLiteral() {
			return getTruth().toBoolean() ? atomToLiteral(getAtom()) : atomToNegatedLiteral(getAtom());
		}

		/**
		 * Returns the weakly assigned decision level.
		 * @return the decision level of a previous MBT if it exists, otherwise the decision level of this entry.
		 */
		default int getWeakDecisionLevel() {
			return hasPreviousMBT() ? getMBTDecisionLevel() : getDecisionLevel();
		}

		/**
		 * Returns the strongly assigned decision level.
		 * @return the decision level of this entry if it is TRUE/FALSE and -1 otherwise.
		 */
		default int getStrongDecisionLevel() {
			return getTruth().isMBT() ? -1 : getDecisionLevel();
		}
	}

	int getDecisionLevel();

	default boolean isViolated(int literal) {
		final int atom = atomOf(literal);
		final ThriceTruth truth = getTruth(atom);

		// For unassigned atoms, any literal is not violated.
		return truth != null && isNegated(literal) != truth.toBoolean();

	}

	default boolean violates(NoGood noGood) {
		// Check each NoGood, if it is violated
		for (Integer noGoodLiteral : noGood) {
			if (!isAssigned(atomOf(noGoodLiteral)) || !isViolated(noGoodLiteral)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	/**
	 * Reports how many atoms are assigned to must-be-true currently. If this method returns
	 * zero, the assignment is guaranteed to be free of must-be-true values (i.e. it only
	 * contains assignments to either true or false).
	 * @return the count of must-be-true values in the asignment.
	 */
	int getMBTCount();

	void backtrack();

	void growForMaxAtomId(int maxAtomId);
}
