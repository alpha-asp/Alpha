package at.ac.tuwien.kr.alpha.solver;

import java.util.*;

public class SimpleAssignmentImpl implements SimpleAssignment<BooleanTruth> {
	private HashMap<Integer, Boolean> delegate = new HashMap<>();
	private LinkedList<SimpleReadableAssignment.Entry<BooleanTruth>> newAssignments2 = new LinkedList<>();

	@Override
	public boolean isAssigned(int atom) {
		return delegate.containsKey(atom);
	}

	@Override
	public BooleanTruth getTruth(int atom) {
		Boolean truth = delegate.get(atom);

		if (truth == null) {
			return null;
		}

		return BooleanTruth.valueOf(truth);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean assign(int atom, BooleanTruth value) {
		return assign(atom, value.toBoolean());
	}

	@Override
	public void unassign(int atom) {
		delegate.remove(atom);
	}

	public boolean assign(int atom, boolean value) {
		newAssignments2.add(new Entry(atom, value));
		delegate.put(atom, value);
		return true;
	}

	@Override
	public Iterator<Map.Entry<Integer, BooleanTruth>> iterator() {
		return delegate.entrySet().stream()
			.map(e -> (Map.Entry<Integer, BooleanTruth>) new AbstractMap.SimpleEntry<>(e.getKey(), BooleanTruth.valueOf(e.getValue())))
			.iterator();
	}

	@Override
	public Iterator<SimpleReadableAssignment.Entry<BooleanTruth>> getNewAssignmentsIterator2() {
		Iterator<SimpleReadableAssignment.Entry<BooleanTruth>> it = newAssignments2.iterator();
		newAssignments2 = new LinkedList<>();
		return it;
	}

	private static class Entry implements SimpleReadableAssignment.Entry<BooleanTruth> {
		private final int atom;
		private final BooleanTruth truth;

		private Entry(int atom, boolean truth) {
			this.atom = atom;
			this.truth = BooleanTruth.valueOf(truth);
		}

		@Override
		public int getAtom() {
			return atom;
		}

		@Override
		public BooleanTruth getTruth() {
			return truth;
		}
	}
}
