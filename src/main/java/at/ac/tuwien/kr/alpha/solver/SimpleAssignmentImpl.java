package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Truth;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleAssignmentImpl implements SimpleAssignment<BooleanTruth> {
	private HashMap<Integer, Boolean> delegate = new HashMap<>();

	@Override
	public boolean isAssigned(int atom) {
		return delegate.containsKey(atom);
	}

	@Override
	public Truth getTruth(int atom) {
		Boolean truth = delegate.get(atom);

		if (truth == null) {
			return null;
		}

		return BooleanTruth.wrap(truth);
	}

	@Override
	public void assign(int atom, BooleanTruth value) {
		assign(atom, value.toBoolean());
	}

	@Override
	public void unassign(int atom) {
		delegate.remove(atom);
	}

	public void assign(int atom, boolean value) {
		delegate.put(atom, value);
	}

	@Override
	public Iterator<Map.Entry<Integer, BooleanTruth>> iterator() {
		return delegate.entrySet().stream()
			.map(e -> (Map.Entry<Integer, BooleanTruth>) new AbstractMap.SimpleEntry<>(e.getKey(), BooleanTruth.wrap(e.getValue())))
			.iterator();
	}
}
