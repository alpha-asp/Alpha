package at.ac.tuwien.kr.alpha.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class BasicAssignment implements Assignment {
	private final Map<Integer, Entry> assignment = new HashMap<>();
	private final List<DecisionLevelState> decisionLevels = new ArrayList<>();
	private final Set<Integer> trueAssignments = new HashSet<>();
	private int mbtCount;

	@Override
	public void clear() {
		trueAssignments.clear();
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
				trueAssignments.remove(atom);
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
	public void assign(int atom, ThriceTruth value, int decisionLevel) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("atom must be positive");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}

		Entry current = get(atom);

		final boolean unassignedToAssigned = current == null;
		final boolean mbtToTrue = !unassignedToAssigned && MBT.equals(current.getTruth()) && TRUE.equals(value);

		/* NOTE(flowlo):
		 * Following code handles what might happen if we're asked to modify an
		 * already existing assignment.
		 * Initially this would throw an IllegalArgumentException, then
		 * exceptions were made for cases when BasicNoGoodStore would assign the
		 * same truth value more than once.
		 * Additionally, DefaultSolver needs to modify truth directly when it
		 * guesses an assignment.
		 * These few lines stay here until we found a resolution on how to treat
		 * that case.
		 * Possible solutions:
		 *  1. Allow overriding (encourages bad code).
		 *  2. Add separate method to "flip" an assignment for guessing.
		 *  3. ?

		if (current != null && current.getTruth().equals(value)) {
			return;
		}

		if (!unassignedToAssigned && !mbtToTrue) {
			throw new IllegalArgumentException("already assigned");
		}

		*/

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
			trueAssignments.add(atom);
			if (mbtToTrue) {
				mbtCount--;
				state.mbtToTrue.put(atom, current.getDecisionLevel());
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
	}

	@Override
	public Set<Integer> getTrueAssignments() {
		return Collections.unmodifiableSet(trueAssignments);
	}

	@Override
	public Entry get(int atom) {
		return assignment.get(atom);
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

	@Override
	public String toString() {
		return Arrays.toString(assignment.entrySet().toArray());
	}

	private class DecisionLevelState {
		final Set<Integer> unassignedToAssigned = new HashSet<>();
		final Map<Integer, Integer> mbtToTrue = new HashMap<>();
	}
}
