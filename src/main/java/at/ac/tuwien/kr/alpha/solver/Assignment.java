/**
 * Copyright (c) 2016, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	default boolean guess(int atom, boolean value) {
		return guess(atom, ThriceTruth.valueOf(value));
	}

	/**
	 * In case that assign fails (i.e., it returns false) the NoGood violated by the assignment can be obtained by this method.
	 * The returned value is arbitrary if the previous assign did not fail.
	 * @return
	 */
	NoGood getNoGoodViolatedByAssign();

	Assignment.Entry getGuessViolatedByAssign();

	void growForMaxAtomId(int maxAtomId);

	Queue<Entry> getAssignmentsToProcess();

	/**
	 * Returns all atomIds that are assigned TRUE in the current assignment.
	 * @return a list of all true assigned atoms.
	 */
	Set<Integer> getTrueAssignments();

	Entry get(int atom);

	int getDecisionLevel();

	/**
	 * Helper for debugging: prints the given NoGood with its assignment inlined.
	 * @param noGood the nogood to print.
	 * @return a string of the nogood with the current assignment (inlined).
	 */
	default String printNoGoodAssignment(NoGood noGood) {
		StringBuilder sb = new StringBuilder();

		sb.append("{ ");

		for (int i = 0; i < noGood.size(); i++) {
			sb.append(noGood.getLiteral(i));
			sb.append("=");
			sb.append(this.get(atomOf(noGood.getLiteral(i))));
			sb.append(" ");
		}

		sb.append("}");

		if (noGood.hasHead()) {
			sb.append("[");
			sb.append(noGood.getHead());
			sb.append("]");
		}

		if (noGood instanceof WatchedNoGood) {
			WatchedNoGood watchedNoGood = (WatchedNoGood) noGood;
			sb.append("{ " + watchedNoGood.getPointer(0) + " " + watchedNoGood.getPointer(1) + " " + watchedNoGood.getAlphaPointer() + " }");
		}

		return sb.toString();
	}

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
	 * @return
	 */
	Iterator<Entry> getNewAssignmentsIterator();

	/**
	 * Returns an iterator over all new assignments. New assignments are only returned once.
	 * getNewAssignmentsIterator and getNewAssignmentsIterator2 are independent of each other (i.e., each has its own backing collection).
	 * @return
	 */
	Iterator<Entry> getNewAssignmentsIterator2();

	interface Entry {
		ThriceTruth getTruth();
		int getDecisionLevel();
		Entry getPrevious();
		NoGood getImpliedBy();

		int getAtom();

		int getPropagationLevel();
		boolean isReassignAtLowerDecisionLevel();
		void setReassignFalse();

		/**
		 * Returns the literal corresponding to this assignment
		 * @return atomId if this entry is TRUE/MBT and -atomId if entry is FALSE.
		 */
		default int getLiteral() {
			return getTruth().toBoolean() ? getAtom() : -getAtom();
		}

		/**
		 * Returns the weakly assigned decision level.
		 * @return the decision level of a previous MBT if it exists, otherwise the decision level of this entry.
		 */
		default int getWeakDecisionLevel() {
			return getPrevious() != null ? getPrevious().getDecisionLevel() : getDecisionLevel();
		}

		/**
		 * Returns the strongly assigned decision level.
		 * @return the decision level of this entry if it is TRUE/FALSE and Integer.MAX_VALUE otherwise.
		 */
		default int getStrongDecisionLevel() {
			return getTruth().isMBT() ? Integer.MAX_VALUE : getDecisionLevel();
		}
	}
}
