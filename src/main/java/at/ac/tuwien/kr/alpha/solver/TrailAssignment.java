/**
 * Copyright (c) 2016-2017, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.AtomTranslator;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

/**
 * An implementation of the Assignment using ArrayList as underlying structure for storing assignment entries.
 */
public class TrailAssignment implements WritableAssignment, Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrailAssignment.class);

	private final AtomTranslator translator;

	private int[] values;
	private int[] strongDecisionLevels;
	private NoGood[] impliedBy;
	private ArrayList<OutOfOrderLiteral> outOfOrderLiterals = new ArrayList<>();
	private int highestDecisionLevelContainingOutOfOrderLiterals;
	private ArrayList<Integer> trail = new ArrayList<>();
	private ArrayList<Integer> trailIndicesOfDecisionLevels = new ArrayList<>();

	private static int nextPositionInTrail;
	private static int newAssignmentsPositionInTrail;
	private static int newAssignmentsIterator;
	private static int assignmentsForChoicePosition;

	private static class OutOfOrderLiteral {
		final int atom;
		final ThriceTruth value;
		final int decisionLevel;
		final NoGood impliedBy;

		private OutOfOrderLiteral(int atom, ThriceTruth value, int decisionLevel, NoGood impliedBy) {
			this.atom = atom;
			this.decisionLevel = decisionLevel;
			this.impliedBy = impliedBy;
			this.value = value;
		}
	}

	private int mbtCount;
	private boolean checksEnabled;

	public TrailAssignment(AtomTranslator translator, boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
		this.translator = translator;
		this.values = new int[0];
		this.strongDecisionLevels = new int[0];
		this.impliedBy = new NoGood[0];
		this.trailIndicesOfDecisionLevels = new ArrayList<>();
		this.trailIndicesOfDecisionLevels.add(0);
	}

	public TrailAssignment(Grounder translator) {
		this(translator, false);
	}

	public TrailAssignment() {
		this(null, false);
	}

	@Override
	public void clear() {
		mbtCount = 0;
		Arrays.fill(values, 0);
		Arrays.fill(strongDecisionLevels, -1);
		outOfOrderLiterals = new ArrayList<>();
		highestDecisionLevelContainingOutOfOrderLiterals = 0;
		trail = new ArrayList<>();
		trailIndicesOfDecisionLevels = new ArrayList<>();
		trailIndicesOfDecisionLevels.add(0);
		nextPositionInTrail = 0;
		newAssignmentsIterator = 0;
		newAssignmentsPositionInTrail = 0;
		assignmentsForChoicePosition = 0;
	}

	@Override
	public boolean isAssigned(int atom) {
		return values[atom] != 0;
	}

	private class TrailPollable implements Pollable<TrailAssignment.Entry> {

		@Override
		public Entry peek() {
			int atom = trail.get(nextPositionInTrail);
			return new Entry(getTruth(atom), atom, getDecisionLevel(), -1, impliedBy[atom]);
		}

		@Override
		public Entry remove() {
			Entry current = peek();
			nextPositionInTrail++;
			return current;
		}

		@Override
		public boolean isEmpty() {
			return nextPositionInTrail >= trail.size();
		}
	}

	@Override
	public Pollable<? extends Assignment.Entry> getAssignmentsToProcess() {
		return new TrailPollable();
	}

	private LinkedList<Assignment.Entry> buildNewAssignmentsFromTrail(int startingPosition) {
		LinkedList<Assignment.Entry> ret = new LinkedList<>();
		int propCount = 0;
		for (int i = startingPosition; i < trail.size(); i++) {
			Integer atom = trail.get(i);
			ret.add(new Entry(getTruth(atom), atom, getDecisionLevel(), propCount++, impliedBy[atom]));
		}
		return ret;
	}

	private void removeLastDecisionLevel() {
		// Remove all atoms recorded in the highest decision level.
		Integer start = trailIndicesOfDecisionLevels.get(getDecisionLevel());
		ListIterator<Integer> backtrackIterator = trail.listIterator(start);
		while (backtrackIterator.hasNext()) {
			Integer backtrackAtom = backtrackIterator.next();
			if (getWeakDecisionLevel(backtrackAtom) < getDecisionLevel()) {
				if (getTruth(backtrackAtom) != TRUE) {
					throw oops("Backtracking assignment with lower decision level whose value is not TRUE.");
				}
				values[backtrackAtom] = (getWeakDecisionLevel(backtrackAtom) << 2) | translateTruth(MBT);
				mbtCount++;
			} else {
				if (getTruth(backtrackAtom) == MBT) {
					mbtCount--;
				}
				values[backtrackAtom] = 0;
			}
			strongDecisionLevels[backtrackAtom] = -1;
		}
		// Remove atoms from trail.
		while (trail.size() > start) {
			trail.remove(trail.size() - 1);
		}
		trailIndicesOfDecisionLevels.remove(trailIndicesOfDecisionLevels.size() - 1);
	}

	private void replayOutOfOrderLiterals() {
		// Replay out-of-order assigned literals.
		if (highestDecisionLevelContainingOutOfOrderLiterals >= getDecisionLevel()) {
			// Replay all still-valid literals and remove invalid ones.
			int k = 0; // counter for out-of-order literals to keep further.
			for (int i = 0; i < outOfOrderLiterals.size(); i++) {
				OutOfOrderLiteral outOfOrderLiteral = outOfOrderLiterals.get(i);
				if (outOfOrderLiteral.decisionLevel <= getDecisionLevel()) {
					// Replay assignment at current decision level.
					assign(outOfOrderLiteral.atom, outOfOrderLiteral.value, outOfOrderLiteral.impliedBy);
					// If literal is actually below current decision level, keep it.
					if (outOfOrderLiteral.decisionLevel < getDecisionLevel()) {
						outOfOrderLiterals.set(k++, outOfOrderLiteral);
					}
				}
			}
			// Remove remaining entries from k onwards.
			while (outOfOrderLiterals.size() > k) {
				outOfOrderLiterals.remove(outOfOrderLiterals.size() - 1);
			}
		}
		highestDecisionLevelContainingOutOfOrderLiterals = getDecisionLevel();
	}

	private void backtrackWithReplayingLowerAssignments() {
		removeLastDecisionLevel();
		// FIXME: when backjumping, the replay is only necessary once (after the last level has been removed), but backjumping is currently not supported by our interfaces.
		nextPositionInTrail = Math.min(nextPositionInTrail, trail.size());
		newAssignmentsPositionInTrail = Math.min(newAssignmentsPositionInTrail, trail.size());
		newAssignmentsIterator = Math.min(newAssignmentsIterator, trail.size());
		assignmentsForChoicePosition = Math.min(assignmentsForChoicePosition, trail.size());
		replayOutOfOrderLiterals();
	}

	@Override
	public void backtrack() {
		backtrackWithReplayingLowerAssignments();
	}

	@Override
	public int getMBTCount() {
		return mbtCount;
	}

	@Override
	public ConflictCause choose(int atom, ThriceTruth value) {
		trailIndicesOfDecisionLevels.add(trail.size());
		return assign(atom, value, null);
	}

	@Override
	public ConflictCause assign(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (decisionLevel < getDecisionLevel()) {
			outOfOrderLiterals.add(new OutOfOrderLiteral(atom, value, decisionLevel, impliedBy));
			if (highestDecisionLevelContainingOutOfOrderLiterals < decisionLevel) {
				highestDecisionLevelContainingOutOfOrderLiterals = decisionLevel;
			}
		}
		ConflictCause conflictCause = assignWithTrail(atom, value, impliedBy);
		if (conflictCause != null) {
			LOGGER.debug("Assign is conflicting: atom: {}, value: {}, impliedBy: {}.", atom, value, impliedBy);
		}
		if (checksEnabled) {
			runInternalChecks();
		}
		return conflictCause;
	}

	private boolean assignmentsConsistent(ThriceTruth oldTruth, ThriceTruth value) {
		return oldTruth == null || oldTruth.toBoolean() == value.toBoolean();
	}

	private ConflictCause assignWithTrail(int atom, ThriceTruth value, NoGood impliedBy) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("not an atom");
		}
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Recording assignment {}={}@{} impliedBy: {}", atom, value, getDecisionLevel(), impliedBy);
			if (impliedBy != null) {
				for (Integer literal : impliedBy) {
					LOGGER.trace("NoGood impliedBy literal assignment: {}={}.", atomOf(literal), get(atomOf(literal)));
				}
			}
		}

		// If the atom currently is not assigned, simply record the assignment.
		final ThriceTruth currentTruth = getTruth(atom);
		if (currentTruth == null) {
			trail.add(atom);
			values[atom] = (getDecisionLevel() << 2) | translateTruth(value);
			// Adjust MBT counter.
			if (value == MBT) {
				mbtCount++;
			}
			return null;
		}

		// Nothing to do if the new value is the same as the current one (or current is TRUE and new is MBT).
		if (value == currentTruth || value == MBT && currentTruth == TRUE) {
			return null;
		}

		// Check if the new assignments contradicts the current one.
		if (!assignmentsConsistent(currentTruth, value)) {
			// Assignments are inconsistent, prepare the reason.
			// TODO: check if this really suffices now.
			return new ConflictCause(impliedBy);
		}

		// Previous assignment exists, and the new one is consistent with it.
		// There is nothing to do except if MBT becomes TRUE.
		if (currentTruth == MBT && value == TRUE) {
			// Record new truth value but keep weak decision level unchanged.
			trail.add(atom);
			values[atom] = (getWeakDecisionLevel(atom) << 2) | translateTruth(TRUE);
			// Record strong decision level.
			strongDecisionLevels[atom] = getDecisionLevel();
			// Adjust MBT counter.
			mbtCount--;
		}
		return null;
	}

	private ThriceTruth translateTruth(int value) {
		switch (value & 0x3) {
			case 0:
				return null;
			case 1:
				return FALSE;
			case 2:
				return MBT;
			case 3:
				return TRUE;
			default:
				throw oops("Unknown truth value.");
		}
	}

	private int translateTruth(ThriceTruth value) {
		if (value == null) {
			return 0;
		}
		switch (value) {
			case FALSE:
				return 1;
			case MBT:
				return 2;
			case TRUE:
				return 3;
		}
		throw oops("Unknown truth value.");
	}

	@Override
	public ThriceTruth getTruth(int atom) {
		return translateTruth(values[atom]);
	}

	@Override
	public int getWeakDecisionLevel(int atom) {
		return values[atom] >> 2;
	}

	@Override
	public int getStrongDecisionLevel(int atom) {
		return strongDecisionLevels[atom];
	}

	@Override
	public Set<Integer> getTrueAssignments() {
		Set<Integer> result = new HashSet<>();
		for (int i = 0; i < values.length; i++) {
			if (getTruth(i) == TRUE) {
				result.add(i);
			}
		}
		return result;
	}

	@Override
	public Entry get(int atom) {
		if (values[atom] == 0) {
			return null;
		}
		if (strongDecisionLevels[atom] == -1) {
			return new Entry(getTruth(atom), atom, getDecisionLevel(), -1, impliedBy[atom]);
		} else {
			return new Entry(getTruth(atom), atom, strongDecisionLevels[atom], -1, impliedBy[atom], getWeakDecisionLevel(atom));
		}
	}

	private void runInternalChecks() {
		// Ensure that truth values in assignment entries agree with those in values array.
		LOGGER.trace("Checking assignment.");
		if (getMBTCount() != getMBTAssignedAtoms().size()) {
			throw oops("MBT counter and amount of actually MBT-assigned atoms disagree");
		} else {
			LOGGER.trace("MBT count agrees with amount of MBT-assigned atoms.");
		}
		LOGGER.trace("Checking assignment: all good.");
	}

	/**
	 * Debug helper collecting all atoms that are assigned MBT.
	 * @return a list of all atomIds that are assigned MBT (and not TRUE).
	 */
	private List<Integer> getMBTAssignedAtoms() {
		List<Integer> ret = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			if (getTruth(i) == MBT) {
				ret.add(i);
			}
		}
		return ret;
	}

	@Override
	public void growForMaxAtomId(int maxAtomId) {
		/*if (assignment.size() > maxAtomId) {
			return;
		}
		assignment.ensureCapacity(maxAtomId + 1);
		// Grow backing array with nulls.
		for (int i = assignment.size(); i <= maxAtomId; i++) {
			assignment.add(i, null);
		}*/
		// Grow arrays only if needed.
		if (values.length > maxAtomId) {
			return;
		}
		// Grow by 1.5 current size, except if bigger array is required due to maxAtomId.
		int newCapacity = values.length + (values.length >> 1);
		if (newCapacity < maxAtomId + 1) {
			newCapacity = maxAtomId + 1;
		}
		int[] newValues = new int[newCapacity];
		System.arraycopy(values, 0, newValues, 0, values.length);
		values = newValues;
		int[] newStrongDecisionLevels = new int[newCapacity];
		System.arraycopy(strongDecisionLevels, 0, newStrongDecisionLevels, 0, strongDecisionLevels.length);
		Arrays.fill(newStrongDecisionLevels, strongDecisionLevels.length, newStrongDecisionLevels.length, -1);
		strongDecisionLevels = newStrongDecisionLevels;
		NoGood[] newimpliedBy = new NoGood[newCapacity];
		System.arraycopy(impliedBy, 0, newimpliedBy, 0, impliedBy.length);
		impliedBy = newimpliedBy;
	}

	@Override
	public int getDecisionLevel() {
		return trailIndicesOfDecisionLevels.size() - 1;
		//return atomsAssignedInDecisionLevel.size() - 1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		boolean isFirst = true;
		for (int i = 0; i < values.length; i++) {
			ThriceTruth atomTruth = getTruth(i);
			if (atomTruth == null) {
				continue;
			}
			if (!isFirst) {
				sb.append(", ");
			}
			isFirst = false;
			sb.append(atomTruth);
			sb.append("_");
			if (translator != null) {
				sb.append(translator.atomToString(i));
			} else {
				sb.append(i);
			}
			sb.append("@");
			sb.append(getWeakDecisionLevel(i));
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public AssignmentIterator getNewAssignmentsIterator() {
		AssignmentIterator ret = new AssignmentIterator();
		return ret;
	}

	@Override
	public AssignmentIteratorForChoice getNewAssignmentsForChoice() {
		AssignmentIteratorForChoice ret = new AssignmentIteratorForChoice();
		return ret;
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
	}

	private class AssignmentIterator implements Iterator<Assignment.Entry> {

		@Override
		public boolean hasNext() {
			return newAssignmentsIterator < trail.size();
		}

		@Override
		public Assignment.Entry next() {
			int atom = trail.get(newAssignmentsIterator++);
			return new Entry(getTruth(atom), atom, getDecisionLevel(), -1, impliedBy[atom]);

		}
	}

	private class AssignmentIteratorForChoice implements Iterator<Assignment.Entry> {

		@Override
		public boolean hasNext() {
			return assignmentsForChoicePosition < trail.size();
		}

		@Override
		public Assignment.Entry next() {
			int atom = trail.get(assignmentsForChoicePosition++);
			return new Entry(getTruth(atom), atom, getDecisionLevel(), -1, impliedBy[atom]);

		}
	}

	private static final class Entry implements Assignment.Entry {
		private final ThriceTruth value;
		private final int decisionLevel;
		private final int propagationLevel;
		private final int previousDecisionLevel;
		private final NoGood impliedBy;
		private final int atom;

		Entry(ThriceTruth value, int atom, int decisionLevel, int propagationLevel, NoGood noGood) {
			this(value, atom, decisionLevel, propagationLevel, noGood, -1);
		}

		Entry(ThriceTruth value, int atom, int decisionLevel, int propagationLevel, NoGood impliedBy, int previousDecisionLevel) {
			this.value = value;
			this.decisionLevel = decisionLevel;
			this.propagationLevel = propagationLevel;
			this.impliedBy = impliedBy;
			this.previousDecisionLevel = previousDecisionLevel;
			this.atom = atom;
			if (previousDecisionLevel != -1 && value != TRUE) {
				throw oops("Assignment.Entry instantiated with previous entry set and truth values other than TRUE now and MBT previous");
			}
		}

		@Override
		public ThriceTruth getTruth() {
			return value;
		}

		@Override
		public int getDecisionLevel() {
			return decisionLevel;
		}

		@Override
		public boolean hasPreviousMBT() {
			return previousDecisionLevel != -1;
		}

		@Override
		public int getMBTDecisionLevel() {
			return previousDecisionLevel;
		}

		@Override
		public int getMBTPropagationLevel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public NoGood getMBTImpliedBy() {
			throw new UnsupportedOperationException();
		}

		@Override
		public NoGood getImpliedBy() {
			return impliedBy;
		}

		@Override
		public int getAtom() {
			return atom;
		}

		@Override
		public int getPropagationLevel() {
			if (propagationLevel == -1) {
				throw new UnsupportedOperationException();
			} else {
				return propagationLevel;
			}
		}

		@Override
		public boolean isReassignAtLowerDecisionLevel() {
			return false;
		}

		@Override
		public String toString() {
			return value.toString() + "(DL" + decisionLevel + ", PL" + propagationLevel + ")"
				+ (hasPreviousMBT() ? "MBT(DL" + getMBTDecisionLevel() + ", PL" + getMBTPropagationLevel() + ")" : "");
		}
	}
}
