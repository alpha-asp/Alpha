package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.OrdinaryAssignment;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class BasicAssignment implements Assignment {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAssignment.class);
	private final Map<Integer, Entry> assignment = new HashMap<>();
	private final List<List<Entry>> decisionLevels;
	private final List<BasicAssignmentIterator> iterators = new ArrayList<>();
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
	public void backtrack() {
		for (BasicAssignmentIterator it : iterators) {
			it.backtrack();
		}

		for (Entry entry : decisionLevels.remove(decisionLevels.size() - 1)) {
			Entry current = assignment.get(entry.getAtom());
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
		if (decisionLevel < getDecisionLevel()) {
			LOGGER.debug("AssignSubDL called with lower decision level.");
		}
		return assignWithDecisionLevel(atom, value, impliedBy, decisionLevel);
	}

	@Override
	public boolean assign(int atom, ThriceTruth value, NoGood impliedBy) {
		return assignWithDecisionLevel(atom, value, impliedBy, getDecisionLevel());
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

			// Create and record new assignment entry.
			final int propagationLevel = decisionLevels.get(decisionLevel).size();
			final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, null, atom);
			LOGGER.trace("Recording assignment {}: {}", atom, next);
			decisionLevels.get(decisionLevel).add(next);
			assignment.put(atom, next);
			return true;
		} else {
			// The atom is already assigned, need to check whether the current one is contradictory.

			if (current.getDecisionLevel() <= decisionLevel) {
				// Assignment is for current decision level, or the current assignment is from a lower decision level than the new one.

				final boolean mbtToTrue = MBT.equals(current.getTruth()) && TRUE.equals(value);
				if (mbtToTrue) {
					// MBT becoming true is fine.
					mbtCount--;

					final int propagationLevel = decisionLevels.get(decisionLevel).size();
					final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, current, atom);
					LOGGER.trace("Recording assignment {}: {}", atom, next);
					decisionLevels.get(decisionLevel).add(next);
					assignment.put(atom, next);
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
					final int propagationLevel = decisionLevels.get(decisionLevel).size();
					final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, null, atom);
					LOGGER.trace("Recording new assignment at lower decision level {}: {}@{}", atom, next, decisionLevel);
					decisionLevels.get(decisionLevel).add(next);
					assignment.put(atom, next);

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

									final int propagationLevel = decisionLevels.get(decisionLevel).size();
									final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, previousEntry, atom);
									LOGGER.trace("Recording assignment {}: {}", atom, next);
									decisionLevels.get(decisionLevel).add(next);
									assignment.put(atom, next);
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
	public Iterator<Assignment.Entry> iterator() {
		BasicAssignmentIterator it = new BasicAssignmentIterator();
		iterators.add(it);
		return it;
	}

	@Override
	public Iterator<OrdinaryAssignment> ordinaryIterator() {
		return new OrdinaryBasicAssignmentIterator(iterator());
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

		/*Entry(ThriceTruth value, int decisionLevel, int propagationLevel, NoGood noGood) {
			this(value, decisionLevel, propagationLevel, noGood, null);
		}

		Entry(ThriceTruth value, int decisionLevel, int propagationLevel) {
			this(value, decisionLevel, propagationLevel, null);
		}*/

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

	private class BasicAssignmentIterator implements java.util.Iterator<Assignment.Entry> {
		// TODO: BasicAssignmentIterator needs adaption to new entries added on lower decision levels.
		private int decisionLevel;
		private int index;
		private int prevIndex;

		private void backtrack() {
			if (decisionLevel <= 0) {
				return;
			}
			if (decisionLevel != decisionLevels.size() - 1) {
				return;
			}
			this.decisionLevel--;
			this.index = prevIndex;
			this.prevIndex = 0;
		}

		@Override
		public boolean hasNext() {
			// There is at least one more decision level.
			if (decisionLevel < decisionLevels.size() - 1) {
				return true;
			}

			return index < decisionLevels.get(decisionLevel).size();
		}

		@Override
		public Assignment.Entry next() {
			List<Entry> current = decisionLevels.get(decisionLevel);
			if (index == current.size()) {
				decisionLevel++;
				prevIndex = index;
				index = 0;
			}
			return decisionLevels.get(decisionLevel).get(index++);
		}
	}

	private class OrdinaryBasicAssignmentIterator implements Iterator<OrdinaryAssignment> {
		private final Iterator<Assignment.Entry> delegate;


		private OrdinaryBasicAssignmentIterator(Iterator<Assignment.Entry> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public OrdinaryAssignment next() {
			Assignment.Entry entry = delegate.next();
			return new OrdinaryAssignment(entry.getAtom(), entry.getTruth().toBoolean());
		}
	}
}
