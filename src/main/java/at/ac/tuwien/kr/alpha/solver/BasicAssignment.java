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

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

public class BasicAssignment implements Assignment {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAssignment.class);
	private final Map<Integer, Entry> assignment = new HashMap<>();
	private final List<List<Entry>> decisionLevels;
	private final ArrayList<Integer> propagationCounterPerDecisionLevel;
	private final Queue<Assignment.Entry> assignmentsToProcess = new LinkedList<>();
	private Queue<Assignment.Entry> newAssignments = new LinkedList<>();
	private Queue<Assignment.Entry> newAssignments2 = new LinkedList<>();
	private final Grounder grounder;

	private int mbtCount;

	public BasicAssignment(Grounder grounder) {
		this.grounder = grounder;
		this.decisionLevels = new ArrayList<>();
		this.decisionLevels.add(new ArrayList<>());
		this.propagationCounterPerDecisionLevel = new ArrayList<>();
		this.propagationCounterPerDecisionLevel.add(0);
	}

	public BasicAssignment() {
		this(null);
	}

	@Override
	public void clear() {
		decisionLevels.clear();
		decisionLevels.add(new ArrayList<>());
		assignment.clear();
		mbtCount = 0;
	}

	@Override
	public Queue<Assignment.Entry> getAssignmentsToProcess() {
		return assignmentsToProcess;
	}

	@Override
	public void backtrack() {
		// Remove all assignments on the current decision level from the queue of assignments to process.
		HashSet<Integer> removedEntries = new HashSet<>();
		for (Iterator<Assignment.Entry> iterator = assignmentsToProcess.iterator(); iterator.hasNext();) {
			Assignment.Entry entry = iterator.next();
			if (entry.getDecisionLevel() == getDecisionLevel()) {
				iterator.remove();
				removedEntries.add(entry.getAtom());
			}
		}
		// If backtracking removed the first assigning entry, any reassignment becomes an ordinary (first) assignment.
		for (Assignment.Entry entry : assignmentsToProcess) {
			// NOTE: this check is most often not needed, perhaps there is a better way to realize this check?
			if (entry.isReassignAtLowerDecisionLevel() && removedEntries.contains(entry.getAtom())) {
				entry.setReassignFalse();
			}
		}
		for (Iterator<Assignment.Entry> iterator = newAssignments.iterator(); iterator.hasNext();) {
			Assignment.Entry entry = iterator.next();
			if (entry.getDecisionLevel() == getDecisionLevel()) {
				iterator.remove();
			}
		}
		for (Iterator<Assignment.Entry> iterator = newAssignments2.iterator(); iterator.hasNext();) {
			Assignment.Entry entry = iterator.next();
			if (entry.getDecisionLevel() == getDecisionLevel()) {
				iterator.remove();
			}
		}

		for (Entry entry : decisionLevels.remove(decisionLevels.size() - 1)) {
			Entry current = assignment.get(entry.getAtom());
			if (current == null) {
				throw new RuntimeException("Entry not in current assignment. Should not happen.");
			}
			// If assignment was moved to lower decision level, do not remove it while backtracking from previously higher decision level.
			if (current.getDecisionLevel() < entry.getDecisionLevel()) {
				continue;
			}
			Entry previous = current.getPrevious();

			if (previous != null && MBT.equals(previous.getTruth()) && TRUE.equals(current.getTruth())) {
				mbtCount++;
				assignment.put(entry.getAtom(), previous);
				LOGGER.trace("Backtracking assignment: {}={} restored to {}={}.", entry.getAtom(), current, entry.getAtom(), previous);
				// TODO: if atom was watched and MBT, the watch might not point to the highest assigned literal now.
			} else {
				if (MBT.equals(current.getTruth())) {
					mbtCount--;
				}
				assignment.remove(entry.getAtom());
				LOGGER.trace("Backtracking assignment: {}={} removed.", entry.getAtom(), entry);
			}
		}

		if (decisionLevels.isEmpty()) {
			decisionLevels.add(new ArrayList<>());
		}
		propagationCounterPerDecisionLevel.remove(propagationCounterPerDecisionLevel.size() - 1);
	}

	@Override
	public int getMBTCount() {
		return mbtCount;
	}

	@Override
	public boolean guess(int atom, ThriceTruth value) {
		decisionLevels.add(new ArrayList<>());
		propagationCounterPerDecisionLevel.add(0);
		return assign(atom, value, null);
	}

	@Override
	public boolean assign(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (decisionLevel > getDecisionLevel() || decisionLevel < 0) {
			throw new IllegalArgumentException("Given decisionLevel is outside range of possible decision levels. Given decisionLevel is: " + decisionLevel);
		}
		if (decisionLevel < getDecisionLevel() && LOGGER.isDebugEnabled()) {
			String atomString = grounder != null ? grounder.atomToString(atom) : Integer.toString(atom);
			LOGGER.trace("Assign called with lower decision level. Atom: {}_{}@{}.", value, atomString, decisionLevel);
		}
		boolean isConflictFree = assignWithDecisionLevel(atom, value, impliedBy, decisionLevel);
		if (!isConflictFree) {
			LOGGER.debug("Assign is conflicting: atom: {}, value: {}, impliedBy: {}.", atom, value, impliedBy);
		}
		return isConflictFree;
	}

	@Override
	public boolean assign(int atom, ThriceTruth value, NoGood impliedBy) {
		boolean isConflictFree = assignWithDecisionLevel(atom, value, impliedBy, getDecisionLevel());
		if (!isConflictFree) {
			LOGGER.debug("Assign is conflicting: atom: {}, value: {}, impliedBy: {}.", atom, value, impliedBy);
		}
		return isConflictFree;
	}

	private NoGood violatedByAssign;

	@Override
	public NoGood getNoGoodViolatedByAssign() {
		return violatedByAssign;
	}

	private Entry guessViolatedByAssign;

	@Override
	public Assignment.Entry getGuessViolatedByAssign() {
		return guessViolatedByAssign;
	}

	@Override
	public void growForMaxAtomId(int maxAtomId) {
		// Nothing to do here, HashMap grows automatically and returns null if entry is not found.
	}

	private boolean assignWithDecisionLevel(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("not an atom");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		if (decisionLevel < getDecisionLevel()) {
			LOGGER.debug("Assign on lower-than-current decision level: atom: {}, decisionLevel: {}, value: {}.", atom, decisionLevel, value);
		}

		final Entry current = get(atom);
		// Check whether the atom is assigned.
		if (current == null) {
			// Atom is unassigned.

			// If assigned value is MBT, increase counter.
			if (MBT.equals(value)) {
				mbtCount++;
			}
			recordAssignment(atom, value, impliedBy, decisionLevel, null);
			return true;
		} else {
			// The atom is already assigned, need to check whether the current one is contradictory.

			if (current.getDecisionLevel() <= decisionLevel) {
				// Assignment is for current decision level, or the current assignment is from a lower decision level than the new one.

				final boolean mbtToTrue = MBT.equals(current.getTruth()) && TRUE.equals(value);
				if (mbtToTrue) {
					// MBT becoming true is fine.
					mbtCount--;

					recordAssignment(atom, value, impliedBy, decisionLevel, current);
					return true;
				} else if (current.getTruth() == value || (TRUE.equals(current.getTruth()) && MBT.equals(value))) {
					LOGGER.trace("Skipping assignment of {} with {} at {}, currently is: {}", atom, decisionLevel, value, current);
					// Skip if the assigned truth value already has been assigned, or if the value is already TRUE and MBT is to be assigned.
					return true;
				} else {
					// All other cases are contradictory.
					violatedByAssign = impliedBy;
					return false;
				}
			} else {
				// Assignment is for lower-than-current decision level.

				// Compute previous truth value.
				ThriceTruth previousTruthValue = null;
				final Entry previousEntry = current.getPrevious();
				if (previousEntry != null && previousEntry.getDecisionLevel() <= decisionLevel) {
					// Previous entry was holding on the decision level of the new assignment.
					previousTruthValue = previousEntry.getTruth();
				}

				// Switch on one of the 21 (possible) combinations of:
				// current assignment, assignment on the lower decision level, and new assignment.

				// If the atom was unassigned at the lower decision level, assign it now to the given value and check for a conflict.
				if (previousTruthValue == null) {
					// Update entry (remove previous, add new)
					// For MBT assigned below TRUE, keep TRUE and modify its previous entry to MBT
					if (TRUE.equals(current.getTruth()) &&  MBT.equals(value)) {
						LOGGER.debug("Updating current assignment {}: {} new MBT below at {}, impliedBy: {}.", atom, current, decisionLevel, impliedBy);
						recordMBTBelowTrue(atom, value, impliedBy, decisionLevel);
					} else {
						LOGGER.trace("Removing current assignment {}: {}", atom, current);
						decisionLevels.get(current.decisionLevel).remove(current);

						// Increment mbtCount in case the assigned value is MBT and current one is not.
						if (MBT.equals(value) && !MBT.equals(current.getTruth())) {
							mbtCount++;
						}

						// Decrement mbtCount in case the assigned value is non-MBT while the current one is.
						if (!MBT.equals(value) && MBT.equals(current.getTruth())) {
							mbtCount--;
						}

						// Add new assignment entry.
						recordAssignment(atom, value, impliedBy, decisionLevel, null);
					}

					// Check whether the assignment on the lower decision level now contradicts the current one.
					switch (current.getTruth()) {
						case FALSE:
							if (MBT.equals(value) || TRUE.equals(value)) {
								violatedByAssign = current.getImpliedBy();
								if (violatedByAssign == null) {
									guessViolatedByAssign = current;
								}
								return false;
							} else {
								return true;
							}
						case MBT:
						case TRUE:
							if (FALSE.equals(value)) {
								violatedByAssign = current.getImpliedBy();
								if (violatedByAssign == null) {
									guessViolatedByAssign = current;
								}
								return false;
							} else {
								return true;
							}
					}
				} else {
					// Previous truth value was assigned.

					// Switch first on current truth value:
					switch (current.getTruth()) {
						case FALSE:
							// Previous truth value must be false.
							if (!FALSE.equals(previousTruthValue)) {
								throw new RuntimeException("Encountered impossible combination of current and previous truth values. Error in algorithm.");
							}
							if (FALSE.equals(value)) {
								return true;
							} else {
								violatedByAssign = impliedBy;
								return false;
							}
						case MBT:
							// Previous truth value must be MBT.
							if (!MBT.equals(previousTruthValue)) {
								throw new RuntimeException("Encountered impossible combination of current and previous truth values. Error in algorithm.");
							}
							switch (value) {
								case FALSE:
									violatedByAssign = impliedBy;
									return false;
								case MBT:
									return true;
								case TRUE:
									// MBT to TRUE, on lower decision level.
									mbtCount--;

									recordAssignment(atom, value, impliedBy, decisionLevel, previousEntry);
									return true;
							}
						case TRUE:
							switch (previousTruthValue) {
								case FALSE:
									violatedByAssign = impliedBy;
									return false;
								case MBT:
								case TRUE:
									return true;
							}
					}
				}
			}
		}
		throw new RuntimeException("Statement should be unreachable, algorithm misses some case.");
	}

	private void recordMBTBelowTrue(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		Entry oldEntry = get(atom);
		if (!TRUE.equals(oldEntry.getTruth()) || !MBT.equals(value)) {
			throw new RuntimeException("Recording MBT below TRUE but truth values do not match. Should not happen.");
		}
		final int previousPropagationLevel = decisionLevels.get(decisionLevel).size();
		final Entry previous = new Entry(value, decisionLevel, previousPropagationLevel, impliedBy, null, atom, true);
		decisionLevels.get(decisionLevel).add(previous);
		assignmentsToProcess.add(previous); // Process MBT on lower decision level.
		// Replace the current TRUE entry with one where previous is set correctly.
		decisionLevels.get(oldEntry.getDecisionLevel()).remove(oldEntry);
		Entry trueEntry = new Entry(oldEntry.getTruth(), oldEntry.getDecisionLevel(), oldEntry.getPropagationLevel(), oldEntry.getImpliedBy(), previous, atom, oldEntry.isReassignAtLowerDecisionLevel());
		decisionLevels.get(oldEntry.getDecisionLevel()).add(trueEntry);
		assignment.put(atom, trueEntry);
	}

	private void recordAssignment(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel, Entry previous) {
		Entry oldEntry = get(atom);
		if (oldEntry != null && decisionLevel >= oldEntry.getDecisionLevel() && !(TRUE.equals(value) && MBT.equals(oldEntry.getTruth()))) {
			throw new RuntimeException("Assigning value into higher decision level. Should not happen.");
		}
		if (previous != null && (!TRUE.equals(value) || !MBT.equals(previous.getTruth()))) {
			throw new RuntimeException("Assignment has previous value, but truth values are not MBT (previously) and TRUE (now). Should not happen.");
		}
		// Create and record new assignment entry.
		final int propagationLevel = propagationCounterPerDecisionLevel.get(decisionLevel);
		propagationCounterPerDecisionLevel.set(decisionLevel, propagationLevel + 1);
		final boolean isReassignAtLowerDecisionLevel = oldEntry != null && oldEntry.getDecisionLevel() > decisionLevel && !isConflicting(oldEntry.getTruth(), value);
		final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, previous, atom, isReassignAtLowerDecisionLevel);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Recording assignment {}: {} impliedBy: {}", atom, next, next.getImpliedBy());
			if (next.getImpliedBy() != null) {
				for (Integer literal : next.getImpliedBy()) {
					LOGGER.trace("NoGood impliedBy literal assignment: {}={}.", atomOf(literal), assignment.get(atomOf(literal)));
				}
			}
		}
		decisionLevels.get(decisionLevel).add(next);
		assignmentsToProcess.add(next);
		newAssignments.add(next);
		newAssignments2.add(next);
		assignment.put(atom, next);
	}

	@Override
	public Set<Integer> getTrueAssignments() {
		Set<Integer> result = new HashSet<>();
		for (Map.Entry<Integer, Entry> entry : assignment.entrySet()) {
			if (TRUE.equals(entry.getValue().getTruth())) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	@Override
	public Entry get(int atom) {
		if (atom < 0) {
			throw new RuntimeException("Requesting entry of negated atom. Should not happen.");
		}
		return assignment.get(atom);
	}

	@Override
	public int getDecisionLevel() {
		return decisionLevels.size() - 1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<Map.Entry<Integer, Entry>> iterator = assignment.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Integer, Entry> assignmentEntry = iterator.next();
			sb.append(assignmentEntry.getValue().getTruth());
			sb.append("_");
			if (grounder != null) {
				sb.append(grounder.atomToString(assignmentEntry.getKey()));
			} else {
				sb.append(assignmentEntry.getKey());
			}
			sb.append("@");
			sb.append(assignmentEntry.getValue().getDecisionLevel());

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Iterator<Assignment.Entry> getNewAssignmentsIterator() {
		Iterator<Assignment.Entry> it = newAssignments.iterator();
		newAssignments = new LinkedList<>();
		return it;
	}

	@Override
	public Iterator<Assignment.Entry> getNewAssignmentsIterator2() {
		Iterator<Assignment.Entry> it = newAssignments2.iterator();
		newAssignments2 = new LinkedList<>();
		return it;
	}

	private static final class Entry implements Assignment.Entry {
		private final ThriceTruth value;
		private final int decisionLevel;
		private final int propagationLevel;
		private final Entry previous;
		private final NoGood impliedBy;
		private final int atom;
		private boolean isReassignAtLowerDecisionLevel;

		Entry(ThriceTruth value, int decisionLevel, int propagationLevel, NoGood noGood, Entry previous, int atom, boolean isReassignAtLowerDecisionLevel) {
			this.value = value;
			this.decisionLevel = decisionLevel;
			this.propagationLevel = propagationLevel;
			this.impliedBy = noGood;
			this.previous = previous;
			this.atom = atom;
			this.isReassignAtLowerDecisionLevel = isReassignAtLowerDecisionLevel;
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
		public NoGood getImpliedBy() {
			return impliedBy;
		}

		@Override
		public Entry getPrevious() {
			return previous;
		}

		@Override
		public int getAtom() {
			return atom;
		}

		@Override
		public int getPropagationLevel() {
			return propagationLevel;
		}

		@Override
		public boolean isReassignAtLowerDecisionLevel() {
			return isReassignAtLowerDecisionLevel;
		}

		@Override
		public void setReassignFalse() {
			this.isReassignAtLowerDecisionLevel = false;
		}

		@Override
		public String toString() {
			return String.format("%d=%s(%d, %d)", atom, value, decisionLevel, propagationLevel);
		}
	}
}
