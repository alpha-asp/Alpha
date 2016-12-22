package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.*;

public class BasicAssignment implements Assignment {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAssignment.class);
	private final Map<Integer, Entry> assignment = new HashMap<>();
	private final List<List<Entry>> decisionLevels;
	private final Queue<Assignment.Entry> assignmentsToProcess = new LinkedList<>();
	private Queue<Assignment.Entry> newAssignments = new LinkedList<>();
	private final Grounder grounder;

	private int mbtCount;

	public BasicAssignment(Grounder grounder) {
		this.grounder = grounder;
		this.decisionLevels = new ArrayList<>();
		this.decisionLevels.add(new ArrayList<>());
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
		for (Iterator<Assignment.Entry> iterator = assignmentsToProcess.iterator(); iterator.hasNext();) {
			Assignment.Entry entry = iterator.next();
			if (entry.getDecisionLevel() == getDecisionLevel()) {
				iterator.remove();
			}
		}
		for (Iterator<Assignment.Entry> iterator = newAssignments.iterator(); iterator.hasNext();) {
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
			} else {
				if (MBT.equals(current.getTruth())) {
					mbtCount--;
				}
				assignment.remove(entry.getAtom());
			}
		}

		if (decisionLevels.isEmpty()) {
			decisionLevels.add(new ArrayList<>());
		}
	}

	@Override
	public int getMBTCount() {
		return mbtCount;
	}

	@Override
	public boolean guess(int atom, ThriceTruth value) {
		decisionLevels.add(new ArrayList<>());
		return assign(atom, value, null);
	}

	@Override
	public boolean assign(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (decisionLevel > getDecisionLevel() || decisionLevel < 0) {
			throw new IllegalArgumentException("Given decisionLevel is outside range of possible decision levels. Given decisionLevel is: " + decisionLevel);
		}
		if (decisionLevel < getDecisionLevel() && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Assign called with lower decision level. Atom: {}_{}@{}.", value, grounder.atomToString(atom), decisionLevel);
		}
		return assignWithDecisionLevel(atom, value, impliedBy, decisionLevel);
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

	private boolean assignWithDecisionLevel(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("not an atom");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
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
					LOGGER.trace("Removing current assignment {}: {}", atom, current);
					decisionLevels.get(current.decisionLevel).remove(current);

					// Increment mbtCount in case the assigned value is MBT and current one is not.
					if (MBT.equals(value) && !MBT.equals(current.getTruth())) {
						mbtCount++;
					}

					// Add new assignment entry.
					recordAssignment(atom, value, impliedBy, decisionLevel, null);

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

	private void recordAssignment(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel, Entry previous) {
		Entry oldEntry = get(atom);
		if (oldEntry != null && decisionLevel >= oldEntry.getDecisionLevel() && !(TRUE.equals(value) && MBT.equals(oldEntry.getTruth()))) {
			throw new RuntimeException("Assigning value into higher decision level. Should not happen.");
		}
		// Create and record new assignment entry.
		final int propagationLevel = decisionLevels.get(decisionLevel).size();
		final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, previous, atom);
		LOGGER.trace("Recording assignment {}: {}", atom, next);
		decisionLevels.get(decisionLevel).add(next);
		assignmentsToProcess.add(next);
		newAssignments.add(next);
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

	private static final class Entry implements Assignment.Entry {
		private final ThriceTruth value;
		private final int decisionLevel;
		private final int propagationLevel;
		private final Entry previous;
		private final NoGood impliedBy;
		private final int atom;

		Entry(ThriceTruth value, int decisionLevel, int propagationLevel, NoGood noGood, Entry previous, int atom) {
			this.value = value;
			this.decisionLevel = decisionLevel;
			this.propagationLevel = propagationLevel;
			this.impliedBy = noGood;
			this.previous = previous;
			this.atom = atom;
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
		public String toString() {
			return value.toString() + "(" + decisionLevel + ", " + propagationLevel + ")";
		}
	}
}
