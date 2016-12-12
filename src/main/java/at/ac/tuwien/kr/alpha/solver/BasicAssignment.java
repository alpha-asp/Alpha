package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.OrdinaryAssignment;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
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
	public boolean assignSubDL(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		// TODO: in case that the assigned value is TRUE while it previously was MBT, the information for backtracking (setting TRUE to either MBT or unassigned) must be adapted.
		if (decisionLevel > getDecisionLevel()) {
			throw new IllegalArgumentException("Given decisionLevel is greater than current one.");
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

	private boolean assignWithDecisionLevel(int atom, ThriceTruth value, NoGood impliedBy, int decisionLevel) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("not an atom");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}

		final Entry current = get(atom);

		if (current != null && (current.getTruth().equals(value) || (TRUE.equals(current.getTruth()) && MBT.equals(value)))) {
			return true;
		}

		final boolean unassignedToAssigned = current == null;
		final boolean mbtToTrue = !unassignedToAssigned && MBT.equals(current.getTruth()) && TRUE.equals(value);

		if (!unassignedToAssigned && !mbtToTrue) {
			return false;
		}

		final int propagationLevel = decisionLevels.get(decisionLevel).size();

		if (mbtToTrue) {
			mbtCount--;
		} else if (MBT.equals(value)) {
			mbtCount++;
		}

		final Entry next = new Entry(value, decisionLevel, propagationLevel, impliedBy, current, atom);
		LOGGER.trace("Recording assignment {}: {}", atom, next);
		decisionLevels.get(decisionLevel).add(next);
		assignment.put(atom, next);
		return true;
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
