package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
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
	private final List<List<Integer>> decisionLevels;
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

		for (Integer atom : decisionLevels.remove(decisionLevels.size() - 1)) {
			Entry entry = assignment.get(atom);
			Entry previous = entry.getPrevious();

			if (previous != null && MBT.equals(previous.getTruth()) && TRUE.equals(entry.getTruth())) {
				mbtCount++;
				assignment.put(atom, previous);
			} else {
				if (MBT.equals(entry.getTruth())) {
					mbtCount--;
				}
				assignment.remove(atom);
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
	public boolean assign(int atom, ThriceTruth value, NoGood impliedBy) {
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

		final int decisionLevel = getDecisionLevel();

		if (mbtToTrue) {
			mbtCount--;
		} else if (MBT.equals(value)) {
			mbtCount++;
		}

		final Entry next = new Entry(value, decisionLevel, impliedBy, current);
		LOGGER.trace("Recording assignment {}: {}", atom, next);
		decisionLevels.get(decisionLevel).add(atom);
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
	public Iterator<Map.Entry<Integer, Assignment.Entry>> iterator() {
		BasicAssignmentIterator it = new BasicAssignmentIterator();
		iterators.add(it);
		return it;
	}

	private static final class Entry implements Assignment.Entry {
		private final ThriceTruth value;
		private final int decisionLevel;
		private final Entry previous;
		private final NoGood impliedBy;

		Entry(ThriceTruth value, int decisionLevel, NoGood noGood, Entry previous) {
			this.value = value;
			this.decisionLevel = decisionLevel;
			this.impliedBy = noGood;
			this.previous = previous;
		}

		Entry(ThriceTruth value, int decisionLevel, NoGood noGood) {
			this(value, decisionLevel, noGood, null);
		}

		Entry(ThriceTruth value, int decisionLevel) {
			this(value, decisionLevel, null);
		}

		@Override
		public ThriceTruth getTruth() {
			return value;
		}

		@Override
		public int getDecisionLevel() {
			return decisionLevel;
		}

		public NoGood getImpliedBy() {
			return impliedBy;
		}

		public Entry getPrevious() {
			return previous;
		}

		@Override
		public String toString() {
			return value.toString() + "(" + decisionLevel + ")";
		}
	}

	private class BasicAssignmentIterator implements java.util.Iterator<Map.Entry<Integer, Assignment.Entry>> {
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
		public Map.Entry<Integer, Assignment.Entry> next() {
			List<Integer> current = decisionLevels.get(decisionLevel);
			if (index == current.size()) {
				decisionLevel++;
				prevIndex = index;
				index = 0;
			}
			final int atom = decisionLevels.get(decisionLevel).get(index++);
			return new AbstractMap.SimpleEntry<>(atom, get(atom));
		}
	}
}
