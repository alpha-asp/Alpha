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
public class ArrayAssignment implements WritableAssignment, Checkable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArrayAssignment.class);

	private final AtomTranslator translator;
	private final ArrayList<Entry> assignment = new ArrayList<>();

	private final List<List<Integer>> atomsAssignedInDecisionLevel;
	private final ArrayList<Integer> propagationCounterPerDecisionLevel;
	private final Queue<ArrayAssignment.Entry> assignmentsToProcess = new LinkedList<>();
	private Queue<Assignment.Entry> newAssignments = new LinkedList<>();
	private Queue<Assignment.Entry> newAssignmentsForChoice = new LinkedList<>();

	private int mbtCount;
	private boolean checksEnabled;

	public ArrayAssignment(AtomTranslator translator, boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
		this.translator = translator;
		this.atomsAssignedInDecisionLevel = new ArrayList<>();
		this.atomsAssignedInDecisionLevel.add(new ArrayList<>());
		this.propagationCounterPerDecisionLevel = new ArrayList<>();
		this.propagationCounterPerDecisionLevel.add(0);
	}

	public ArrayAssignment(Grounder translator) {
		this(translator, false);
	}

	public ArrayAssignment() {
		this(null, false);
	}

	@Override
	public void clear() {
		atomsAssignedInDecisionLevel.clear();
		atomsAssignedInDecisionLevel.add(new ArrayList<>());
		assignment.clear();
		mbtCount = 0;
	}

	@Override
	public Queue<? extends Assignment.Entry> getAssignmentsToProcess() {
		return assignmentsToProcess;
	}

	@Override
	public void backtrack() {
		// Remove all assignments on the current decision level from the queue of assignments to process.
		HashSet<Integer> removedEntries = new HashSet<>();
		for (Iterator<ArrayAssignment.Entry> iterator = assignmentsToProcess.iterator(); iterator.hasNext();) {
			Assignment.Entry entry = iterator.next();
			if (entry.getDecisionLevel() == getDecisionLevel()) {
				iterator.remove();
				removedEntries.add(entry.getAtom());
			}
		}
		// If backtracking removed the first assigning entry, any reassignment becomes an ordinary (first) assignment.
		for (ArrayAssignment.Entry entry : assignmentsToProcess) {
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
		// Hint: it might be faster to just return all assignments for choice and let the ChoiceManager avoid duplicate checks.
		for (Iterator<Assignment.Entry> iterator = newAssignmentsForChoice.iterator(); iterator.hasNext();) {
			Assignment.Entry entry = iterator.next();
			if (entry.getDecisionLevel() == getDecisionLevel() && !entry.isReassignAtLowerDecisionLevel()) {
				iterator.remove();
			}
		}

		int decisionLevelToRemove = atomsAssignedInDecisionLevel.size() - 1;
		for (Integer atom : atomsAssignedInDecisionLevel.remove(decisionLevelToRemove)) {
			Entry current = assignment.get(atom);
			if (current == null) {
				throw oops("Entry not in current assignment");
			}
			// If assignment was moved to lower decision level, do not remove it while backtracking from previously higher decision level.
			if (current.getDecisionLevel() < decisionLevelToRemove) {
				continue;
			}
			Entry previous = current.getPrevious();

			if (previous != null && previous.getDecisionLevel() < decisionLevelToRemove) {
				// Restore previous MBT.
				mbtCount++;
				assignment.set(atom, previous);
				LOGGER.trace("Backtracking assignment: {}={} restored to {}={}.", atom, current, atom, previous);
			} else {
				if (MBT.equals(current.getTruth())) {
					mbtCount--;
				}
				assignment.set(atom, null);
				LOGGER.trace("Backtracking assignment: {}={} removed.", atom, current);
			}
		}

		if (atomsAssignedInDecisionLevel.isEmpty()) {
			atomsAssignedInDecisionLevel.add(new ArrayList<>());
		}
		propagationCounterPerDecisionLevel.remove(propagationCounterPerDecisionLevel.size() - 1);
	}

	@Override
	public int getMBTCount() {
		return mbtCount;
	}

	@Override
	public ConflictCause choose(int atom, ThriceTruth value) {
		atomsAssignedInDecisionLevel.add(new ArrayList<>());
		propagationCounterPerDecisionLevel.add(0);
		return assign(atom, value, null);
	}

	@Override
	public ConflictCause assign(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (decisionLevel > getDecisionLevel() || decisionLevel < 0) {
			throw new IllegalArgumentException("Given decisionLevel is outside range of possible decision levels. Given decisionLevel is: " + decisionLevel);
		}
		if (decisionLevel < getDecisionLevel() && LOGGER.isDebugEnabled()) {
			String atomString = translator != null ? translator.atomToString(atom) : Integer.toString(atom);
			LOGGER.trace("Assign called with lower decision level. Atom: {}_{}@{}.", value, atomString, decisionLevel);
		}
		ConflictCause isConflictFree = assignWithDecisionLevel(atom, value, impliedBy, decisionLevel);
		if (isConflictFree != null) {
			LOGGER.debug("Assign is conflicting: atom: {}, value: {}, impliedBy: {}.", atom, value, impliedBy);
		}
		return isConflictFree;
	}

	private boolean assignmentsConsistent(Assignment.Entry oldAssignment, ThriceTruth value) {
		return oldAssignment == null || oldAssignment.getTruth().toBoolean() == value.toBoolean();
	}

	private ConflictCause assignWithDecisionLevel(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (checksEnabled) {
			if (getMBTCount() != getMBTAssignedAtoms().size()) {
				throw oops("MBT counter and amount of actually MBT-assigned atoms disagree");
			} else {
				LOGGER.trace("MBT count agrees with amount of MBT-assigned atoms.");
			}
		}
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("not an atom");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		if (decisionLevel < getDecisionLevel() && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Assign on lower-than-current decision level: atom: {}, decisionLevel: {}, value: {}.", atom, decisionLevel, value);
		}

		// Check if the new assignments contradicts the current one.
		final Entry current = get(atom);

		// Nothing to do if the new value is the same as the current one (or current is TRUE and new is MBT),
		// and the current one has lower decision level.
		if (current != null && current.getDecisionLevel() <= decisionLevel &&
			(value.equals(current.getTruth()) || value.isMBT() && TRUE.equals(current.getTruth()))) {
			return null;
		}

		// If the atom currently is not assigned, simply record the assignment.
		if (current == null) {
			// If assigned value is MBT, increase counter.
			if (MBT.equals(value)) {
				mbtCount++;
			}
			recordAssignment(atom, value, impliedBy, decisionLevel, null);
			return null;
		}

		// Check consistency.
		if (!assignmentsConsistent(current, value)) {
			ConflictCause conflictCause = new ConflictCause(impliedBy);
			// Assignments are inconsistent, prepare the reason.
			NoGood violated;
			if (decisionLevel < current.getWeakDecisionLevel()) {
				// New assignment is lower than the current one, hence cause is the reason for the (higher) current one.
				violated = current.getPrevious() == null ? current.getImpliedBy() : current.getPrevious().getImpliedBy();	// take MBT reason if it exists.
				if (violated == null) {
					conflictCause = new ConflictCause(current);
				} else {
					conflictCause = new ConflictCause(violated);
				}
				// The lower assignment takes precedence over the current value, overwrite it and adjust mbtCounter.
				if (current.getTruth() == MBT) {
					mbtCount--;
				}
				recordAssignment(atom, value, impliedBy, decisionLevel, null);
				if (value == MBT) {
					mbtCount++;
				}

			}
			return conflictCause;
		}

		// Previous assignment exists, and the new one is consistent with it.
		switch (value) {
			case FALSE:
				// Previous must be false, simply re-assign it.
				recordAssignment(atom, value, impliedBy, decisionLevel, null);
				return null;
			case TRUE:
				ArrayAssignment.Entry eventualPreExistingMBT = current.getTruth().isMBT() ? current : current.getPrevious();
				if (eventualPreExistingMBT != null && eventualPreExistingMBT.getDecisionLevel() <= decisionLevel) {
					// TRUE is below current TRUE but above-or-equal a current MBT.
					recordAssignment(atom, value, impliedBy, decisionLevel, eventualPreExistingMBT);
				} else {
					// TRUE is below current TRUE and below an eventual MBT.
					recordAssignment(atom, value, impliedBy, decisionLevel, null);
				}
				if (current.getTruth().isMBT()) {
					mbtCount--;
				}
				return null;
			case MBT:
				if (current.getPrevious() != null && current.getPrevious().getDecisionLevel() <= decisionLevel
					|| TRUE.equals(current.getTruth()) && current.getDecisionLevel() <= decisionLevel) {
					// New assignment is above-or-equal to an already existing MBT,
					// or current is TRUE and at same decision level as the new MBT. Ignore it.
					return null;
				}
				if (!current.getTruth().isMBT()) {
					// Current assignment is TRUE and new one is MBT below (and lower than a previous MBT).
					LOGGER.debug("Updating current assignment {}: {} new MBT below at {}, impliedBy: {}.", atom, current, decisionLevel, impliedBy);
					recordMBTBelowTrue(atom, value, impliedBy, decisionLevel);
				} else {
					// Current assignment is MBT and the new one is below it (no TRUE above exists).
					recordAssignment(atom, value, impliedBy, decisionLevel, null);
				}
				return null;
		}
		throw oops("Statement should be unreachable, algorithm misses some case");
	}

	private void recordMBTBelowTrue(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		Entry oldEntry = get(atom);
		if (!TRUE.equals(oldEntry.getTruth()) || !MBT.equals(value)) {
			throw oops("Recording MBT below TRUE but truth values do not match");
		}
		//final int previousPropagationLevel = atomsAssignedInDecisionLevel.get(decisionLevel).size();
		final int previousPropagationLevel = propagationCounterPerDecisionLevel.get(decisionLevel);
		propagationCounterPerDecisionLevel.set(decisionLevel, previousPropagationLevel + 1);
		final Entry previous = new Entry(value, decisionLevel, previousPropagationLevel, impliedBy, null, atom, true);
		atomsAssignedInDecisionLevel.get(decisionLevel).add(previous.getAtom());
		assignmentsToProcess.add(previous); // Process MBT on lower decision level.
		// Replace the current TRUE entry with one where previous is set correctly.
		Entry trueEntry = new Entry(oldEntry.getTruth(), oldEntry.getDecisionLevel(), oldEntry.getPropagationLevel(), oldEntry.getImpliedBy(), previous, atom, oldEntry.isReassignAtLowerDecisionLevel());
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Recording assignment {}: MBT below TRUE {} impliedBy: {}", atom, trueEntry.getPrevious(), trueEntry.getPrevious().getImpliedBy());
			if (trueEntry.getPrevious().getImpliedBy() != null) {
				for (Integer literal : trueEntry.getPrevious().getImpliedBy()) {
					LOGGER.trace("NoGood impliedBy literal assignment: {}={}.", atomOf(literal), assignment.get(atomOf(literal)));
				}
			}
		}
		atomsAssignedInDecisionLevel.get(oldEntry.getDecisionLevel()).add(trueEntry.getAtom());
		assignment.set(atom, trueEntry);
	}

	private void recordAssignment(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel, Entry previous) {
		Entry oldEntry = get(atom);
		if (oldEntry != null && decisionLevel >= oldEntry.getDecisionLevel() && !(TRUE.equals(value) && MBT.equals(oldEntry.getTruth()))) {
			throw oops("Assigning value into higher decision level");
		}
		if (previous != null && (!TRUE.equals(value) || !MBT.equals(previous.getTruth()))) {
			throw oops("Assignment has previous value, but truth values are not MBT (previously) and TRUE (now)");
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
		// Record atom for backtracking (avoid duplicate records if MBT and TRUE are assigned on the same decision level).
		if (next.getPrevious() == null || next.getPrevious().getDecisionLevel() < decisionLevel) {
			atomsAssignedInDecisionLevel.get(decisionLevel).add(next.getAtom());
		}
		assignmentsToProcess.add(next);
		newAssignments.add(next);
		newAssignmentsForChoice.add(next);
		assignment.set(atom, next);
	}

	@Override
	public Set<Integer> getTrueAssignments() {
		Set<Integer> result = new HashSet<>();
		for (Entry entry : assignment) {
			if (entry != null && TRUE.equals(entry.getTruth())) {
				result.add(entry.getAtom());
			}
		}
		return result;
	}

	@Override
	public Entry get(int atom) {
		return assignment.get(atom);
	}

	/**
	 * Debug helper collecting all atoms that are assigned MBT.
	 * @return a list of all atomIds that are assigned MBT (and not TRUE).
	 */
	private List<Integer> getMBTAssignedAtoms() {
		List<Integer> ret = new ArrayList<>();
		for (int i = 0; i < assignment.size(); i++) {
			Entry entry = assignment.get(i);
			if (entry != null && entry.getTruth() == MBT) {
				ret.add(i);
			}
		}
		return ret;
	}

	@Override
	public void growForMaxAtomId(int maxAtomId) {
		if (assignment.size() > maxAtomId) {
			return;
		}
		assignment.ensureCapacity(maxAtomId + 1);
		// Grow backing array with nulls.
		for (int i = assignment.size(); i <= maxAtomId; i++) {
			assignment.add(i, null);
		}
	}

	@Override
	public int getDecisionLevel() {
		return atomsAssignedInDecisionLevel.size() - 1;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		boolean isFirst = true;
		for (Entry assignmentEntry : assignment) {
			if (assignmentEntry == null) {
				continue;
			}
			if (!isFirst) {
				sb.append(", ");
			}
			isFirst = false;
			sb.append(assignmentEntry.getTruth());
			sb.append("_");
			if (translator != null) {
				sb.append(translator.atomToString(assignmentEntry.getAtom()));
			} else {
				sb.append(assignmentEntry.getAtom());
			}
			sb.append("@");
			sb.append(assignmentEntry.getDecisionLevel());
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
	public Iterator<Assignment.Entry> getNewAssignmentsForChoice() {
		Iterator<Assignment.Entry> it = newAssignmentsForChoice.iterator();
		newAssignmentsForChoice = new LinkedList<>();
		return it;
	}

	@Override
	public void setChecksEnabled(boolean checksEnabled) {
		this.checksEnabled = checksEnabled;
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
			if (previous != null && !(value == TRUE && previous.value == MBT)) {
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

		public void setReassignFalse() {
			this.isReassignAtLowerDecisionLevel = false;
		}

		@Override
		public String toString() {
			return value.toString() + "(" + decisionLevel + ", " + propagationLevel + ")";
		}
	}
}
