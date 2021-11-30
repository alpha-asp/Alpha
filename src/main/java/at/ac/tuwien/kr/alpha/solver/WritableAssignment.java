/*
 * Copyright (c) 2016-2021, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;

public interface WritableAssignment extends Assignment {
	/**
	 * Delete all information stored in the assignment.
	 */
	void clear();

	/**
	 * Backtracks the most recent decision level.
	 */
	void backtrack();

	/**
	 * Backtracks to the indicated decision level. Every assignment on a higher decisionLevel is removed.
	 * All assignments below (or equal to) decisionLevel are kept. Note that for atoms being TRUE this may require
	 * setting the assigned value to MBT during backtracking.
	 * @param decisionLevel the decision level to backjump to (this decision level is the highest that is kept).
	 */
	void backjump(int decisionLevel);

	/**
	 * Assigns an atom some value on the indicated decision level.
	 * @param atom the atom to assign
	 * @param value the truth value to assign the atom
	 * @param impliedBy the antecedent that is implying the atom.
	 * @param decisionLevel the decision level on which the assignment is to take effect.
	 * @param informCallbacks whether callbacks shall be informed of the assignment.
	 * @return null if the assignment is consistent or a ConflictCause otherwise.
	 */
	ConflictCause assign(int atom, ThriceTruth value, Antecedent impliedBy, int decisionLevel, boolean informCallbacks);

	default ConflictCause assign(int atom, ThriceTruth value, Antecedent impliedBy, int decisionLevel) {
		return assign(atom, value, impliedBy, decisionLevel, true);
	}

	default ConflictCause assign(int atom, ThriceTruth value, Antecedent impliedBy, boolean informCallbacks) {
		return assign(atom, value, impliedBy, getDecisionLevel(), informCallbacks);
	}

	default ConflictCause assign(int atom, ThriceTruth value, Antecedent impliedBy) {
		return assign(atom, value, impliedBy, true);
	}

	default ConflictCause assign(int atom, ThriceTruth value) {
		return assign(atom, value, null, true);
	}

	ConflictCause choose(int atom, ThriceTruth value);

	void registerCallbackOnChange(int atom);

	void setCallback(ChoiceManager choiceManager);

	default ConflictCause choose(int atom, boolean value) {
		return choose(atom, ThriceTruth.valueOf(value));
	}

	default int minimumConflictLevel(NoGood noGood) {
		int minimumConflictLevel = -1;
		for (Integer literal : noGood) {
			ThriceTruth atomTruth = getTruth(atomOf(literal));
			if (atomTruth == null || isPositive(literal) != atomTruth.toBoolean()) {
				return -1;
			}
			int literalDecisionLevel = getWeakDecisionLevel(atomOf(literal));
			if (literalDecisionLevel > minimumConflictLevel) {
				minimumConflictLevel = literalDecisionLevel;
			}
		}
		return minimumConflictLevel;
	}

	/**
	 * Assigns all unassigned atoms to FALSE.
	 * @return true if any atom was assigned.
	 */
	boolean closeUnassignedAtoms();

	/**
	 * Returns whether the assignment did change since this method was last called.
	 * @return true if the assignment changed since this method was last called.
	 */
	boolean didChange();
}
