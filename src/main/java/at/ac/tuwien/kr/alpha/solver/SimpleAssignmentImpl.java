package at.ac.tuwien.kr.alpha.solver;

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
		delegate.put(atom, value);
		return true;
	}

	@Override
	public Iterator<Map.Entry<Integer, BooleanTruth>> iterator() {
		return delegate.entrySet().stream()
			.map(e -> (Map.Entry<Integer, BooleanTruth>) new AbstractMap.SimpleEntry<>(e.getKey(), BooleanTruth.valueOf(e.getValue())))
			.iterator();
	}
}
