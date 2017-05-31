package at.ac.tuwien.kr.alpha.solver;

import java.util.*;

public class SimpleAssignmentImpl implements SimpleAssignment {
	private HashMap<Integer, ThriceTruth> delegate = new HashMap<>();
	private LinkedList<SimpleReadableAssignment.Entry> newAssignments2 = new LinkedList<>();

	@Override
	public boolean isAssigned(int atom) {
		return delegate.containsKey(atom);
	}

	@Override
	public ThriceTruth getTruth(int atom) {
		return delegate.get(atom);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean assign(int atom, ThriceTruth value) {
		newAssignments2.add(new Entry(atom, value));
		delegate.put(atom, value);
		return true;
	}

	@Override
	public void unassign(int atom) {
		delegate.remove(atom);
	}

	public boolean assign(int atom, boolean value) {
		return assign(atom, ThriceTruth.valueOf(value));
	}

	@Override
	public Iterator<Map.Entry<Integer, ThriceTruth>> iterator() {
		return delegate.entrySet().stream()
			.map(e -> (Map.Entry<Integer, ThriceTruth>) new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()))
			.iterator();
	}

	@Override
	public Iterator<SimpleReadableAssignment.Entry> getNewAssignmentsIterator2() {
		Iterator<SimpleReadableAssignment.Entry> it = newAssignments2.iterator();
		newAssignments2 = new LinkedList<>();
		return it;
	}

	private static class Entry implements SimpleReadableAssignment.Entry {
		private final int atom;
		private final ThriceTruth truth;

		private Entry(int atom, ThriceTruth truth) {
			this.atom = atom;
			this.truth = truth;
		}

		@Override
		public int getAtom() {
			return atom;
		}

		@Override
		public ThriceTruth getTruth() {
			return truth;
		}
	}
}
