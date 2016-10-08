package at.ac.tuwien.kr.alpha.solver;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class BasicAssignment implements Assignment {
	private final Map<Integer, Entry> assignment = new HashMap<>();
	private final List<Set<Integer>> decisionLevels = new ArrayList<>();
	private final Set<Integer> trueAssignments = new HashSet<>();
	private int mbts;

	@Override
	public void clear() {
		trueAssignments.clear();
		decisionLevels.clear();
		assignment.clear();
		mbts = 0;
	}

	@Override
	public void backtrack(int decisionLevel) {
		for (int i = decisionLevel + 1; i < decisionLevels.size(); i++) {
			for (Integer atom : decisionLevels.remove(i)) {
				if (MBT.equals(getTruth(atom))) {
					mbts--;
				}
				assignment.remove(atom);
				trueAssignments.remove(atom);
			}
		}
	}

	@Override
	public boolean containsMBT() {
		return mbts > 0;
	}

	@Override
	public void assign(int atom, ThriceTruth value, int decisionLevel) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("atom must be positive");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}

		if (isAssigned(atom)) {
			throw new IllegalArgumentException("already assigned");
		}

		if (decisionLevel == decisionLevels.size()) {
			final HashSet<Integer> set = new HashSet<>();
			set.add(atom);
			decisionLevels.add(set);
		} else if (decisionLevel == decisionLevels.size() - 1) {
			decisionLevels.get(decisionLevel).add(atom);
		} else {
			throw new IllegalArgumentException("Wrong decision level!");
		}

		assignment.put(atom, new Entry(value, decisionLevel));

		if (TRUE.equals(value)) {
			trueAssignments.add(atom);
		} else if (MBT.equals(value)) {
			mbts++;
		}
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
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[ ");
		for (Map.Entry<Integer, Entry> item : assignment.entrySet()) {
			sb.append(item.getKey());
			sb.append(" := ");
			sb.append(item.getValue().getTruth().toString());
			sb.append("(");
			sb.append(item.getValue().getDecisionLevel());
			sb.append("), ");
		}
		sb.append("]");

		return sb.toString();
	}
}
