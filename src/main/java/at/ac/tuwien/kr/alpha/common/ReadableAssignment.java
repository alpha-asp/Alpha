package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.solver.Assignment;
import at.ac.tuwien.kr.alpha.solver.SimpleReadableAssignment;

import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public interface ReadableAssignment<T extends Truth> extends SimpleReadableAssignment<T> {
	/**
	 * Reports how many atoms are assigned to must-be-true currently. If this method returns
	 * zero, the assignment is guaranteed to be free of must-be-true values (i.e. it only
	 * contains assignments to either true or false).
	 * @return the count of must-be-true values in the asignment.
	 */
	int getMBTCount();

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	Queue<Assignment.Entry<T>> getAssignmentsToProcess();
	Assignment.Entry getGuessViolatedByAssign();
	NoGood getNoGoodViolatedByAssign();

	/**
	 * Returns an iterator over all new assignments. New assignments are only returned once.
	 * @return
	 */
	Iterator<Assignment.Entry<T>> getNewAssignmentsIterator();

	Iterator<Assignment.Entry<T>> getNewAssignmentsIterator2();

	Entry<T> get(int atom);

	int getDecisionLevel();

	/**
	 * Returns the truth value assigned to an atom.
	 * @param atom the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	default T getTruth(int atom) {
		final Entry<T> entry = get(atom);
		return entry == null ? null : entry.getTruth();
	}

	default boolean isAssigned(int atom) {
		return get(atom) != null;
	}

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && (isNegated(literal) ? FALSE : TRUE).equals(entry.getTruth());
	}

	default boolean containsWeakComplement(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && isNegated(literal) == !entry.getTruth().toBoolean();
	}

	default boolean containsWeakComplement(NoGood noGood, int index) {
		return containsWeakComplement(noGood.getLiteral(index));
	}

	default boolean contains(NoGood noGood, int index) {
		return contains(noGood.getLiteral(index));
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

	interface Entry<T extends Truth> {
		T getTruth();
		int getDecisionLevel();
		Entry<T> getPrevious();
		NoGood getImpliedBy();

		int getAtom();

		int getPropagationLevel();
		boolean isReassignAtLowerDecisionLevel();
	}
}
