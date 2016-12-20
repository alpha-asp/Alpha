package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public interface Assignment {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	/**
	 * Backtracks to the indicated decision level. Every assignment on a higher decisionLevel is removed.
	 * All assignments below (or equal to) decisionLevel are kept. Note that for atoms being TRUE this may require
	 * setting the assigned value to MBT during backtracking.
	 */
	void backtrack();

	/**
	 * Reports how many atoms are assigned to must-be-true currently. If this method returns
	 * zero, the assignment is guaranteed to be free of must-be-true values (i.e. it only
	 * contains assignments to either true or false).
	 * @return the count of must-be-true values in the asignment.
	 */
	int getMBTCount();

	boolean assign(int atom, ThriceTruth value, NoGood impliedBy);

	/**
	 * Assigns an atom some value on a lower decision level than the current one.
	 * @param atom
	 * @param value
	 * @param impliedBy
	 * @param decisionLevel
	 * @return
	 */
	boolean assign(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel);

	default boolean assign(int atom, ThriceTruth value) {
		return assign(atom, value, null);
	}

	boolean guess(int atom, ThriceTruth value);

	/**
	 * In case that assign fails (i.e., it returns false) the NoGood violated by the assignment can be obtained by this method.
	 * The returned value is arbitrary if the previous assign did not fail.
	 * @return
	 */
	NoGood getNoGoodViolatedByAssign();

	Assignment.Entry getGuessViolatedByAssign();

	Queue<Entry> getAssignmentsToProcess();

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	Entry get(int atom);

	int getDecisionLevel();

	/**
	 * Returns the truth value assigned to an atom.
	 * @param atom the id of the atom.
	 * @return the truth value; null if atomId is not assigned.
	 */
	default ThriceTruth getTruth(int atom) {
		final Entry entry = get(atom);
		return entry == null ? null : entry.getTruth();
	}

	default boolean isAssigned(int atom) {
		return get(atom) != null;
	}

	default boolean containsWeakComplement(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && isNegated(literal) == !entry.getTruth().toBoolean();
	}

	default boolean containsWeakComplement(NoGood noGood, int index) {
		return containsWeakComplement(noGood.getLiteral(index));
	}

	default boolean contains(int literal) {
		final Entry entry = get(atomOf(literal));
		return entry != null && (isNegated(literal) ? FALSE : TRUE).equals(entry.getTruth());
	}

	default boolean contains(NoGood noGood, int index) {
		return contains(noGood.getLiteral(index));
	}

	/**
	 * Returns an iterator over all new assignments. New assignments are only returned once.
	 * @return
	 */
	Iterator<Entry> getNewAssignmentsIterator();

	interface Entry {
		ThriceTruth getTruth();
		int getDecisionLevel();
		Entry getPrevious();
		NoGood getImpliedBy();

		int getAtom();

		int getPropagationLevel();
	}
}
