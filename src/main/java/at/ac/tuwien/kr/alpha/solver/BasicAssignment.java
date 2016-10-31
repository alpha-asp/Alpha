package at.ac.tuwien.kr.alpha.solver;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class BasicAssignment implements Assignment {
	private final Map<Integer, Entry> assignment = new HashMap<>();
	private final List<DecisionLevelState> decisionLevels = new ArrayList<>();

	private int mbtCount;

	@Override
	public void clear() {
		decisionLevels.clear();
		assignment.clear();
		mbtCount = 0;
	}

	@Override
	public void backtrack(int decisionLevel) {
		for (int i = decisionLevel + 1; i < decisionLevels.size(); i++) {
			DecisionLevelState state = decisionLevels.remove(i);

			for (Map.Entry<Integer, Integer> entry : state.mbtToTrue.entrySet()) {
				final int atom = entry.getKey();
				mbtCount++;
				assignment.put(atom, new Entry(MBT, entry.getValue()));
			}

			for (Integer atom : state.unassignedToAssigned) {
				if (MBT.equals(getTruth(atom))) {
					mbtCount--;
				}
				assignment.remove(atom);
			}
		}
	}

	@Override
	public int getMBTCount() {
		return mbtCount;
	}

	@Override
	public boolean assign(int atom, ThriceTruth value, int decisionLevel) {
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

		DecisionLevelState state;

		// If we're at a new decision level, set up state.
		if (decisionLevel == decisionLevels.size()) {
			state = new DecisionLevelState();
			decisionLevels.add(state);
		} else if (decisionLevel == decisionLevels.size() - 1) {
			state = decisionLevels.get(decisionLevel);
		} else {
			throw new IllegalArgumentException("Wrong decision level!");
		}

		if (TRUE.equals(value)) {
			if (mbtToTrue) {
				mbtCount--;
				state.mbtToTrue.put(atom, current.getDecisionLevel());
			} else {
				state.unassignedToAssigned.add(atom);
			}
		} else {
			if (MBT.equals(value)) {
				mbtCount++;
			}
			if (unassignedToAssigned) {
				state.unassignedToAssigned.add(atom);
			}
		}

		assignment.put(atom, new Entry(value, decisionLevel));
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
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<Map.Entry<Integer, Entry>> iterator = assignment.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Integer, Entry> assignmentEntry = iterator.next();
			sb.append(assignmentEntry.getValue().getTruth());
			sb.append("_");
			sb.append(assignmentEntry.getKey());
			sb.append("@");
			sb.append(assignmentEntry.getValue().getDecisionLevel());

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	private static final class Entry implements Assignment.Entry {
		private final ThriceTruth value;
		private final int decisionLevel;

		Entry(ThriceTruth value, int decisionLevel) {
			this.value = value;
			this.decisionLevel = decisionLevel;
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
		public String toString() {
			return value.toString() + "(" + decisionLevel + ")";
		}
	}

	private static final class DecisionLevelState {
		private final Set<Integer> unassignedToAssigned = new HashSet<>();
		private final Map<Integer, Integer> mbtToTrue = new HashMap<>();
	}
}
