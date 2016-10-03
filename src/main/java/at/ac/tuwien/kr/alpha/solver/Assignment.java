package at.ac.tuwien.kr.alpha.solver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.Literals.isNegated;
import static at.ac.tuwien.kr.alpha.common.Atoms.isAtom;

public class Assignment<V extends Truth> {
	private static final Log LOG = LogFactory.getLog(Assignment.class);

	private final Map<Integer, AtomAssignment<V>> assignment;

	public Assignment() {
		this.assignment = new HashMap<>();
	}

	void clear() {
		assignment.clear();
	}

	public void assign(int atom, V value, int decisionLevel) {
		if (!isAtom(atom)) {
			throw new IllegalArgumentException("atom must be positive");
		}

		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}

		final AtomAssignment<V> oldAssignment = assignment.get(atom);

		if (oldAssignment != null) {
			LOG.debug("Changing assignment of atom " + atom + " from " + oldAssignment.getValue() + " to " + value);
		} else {
			LOG.debug("Assigning previously unassigned atom " + atom + " to " + value);
		}

		assignment.put(atom, new AtomAssignment<V>(value, decisionLevel));
	}

	public boolean isAssigned(int atom) {
		return assignment.containsKey(atom);
	}

	public int toPriority(int atom) {
		final AtomAssignment atomAssignment = assignment.get(atom);

		if (atomAssignment == null) {
			return Integer.MAX_VALUE;
		}
		return atomAssignment.getDecisionLevel();
	}

	public V get(int atom) {
		AtomAssignment<V> atomAssignment = assignment.get(atom);
		if (atomAssignment != null) {
			return atomAssignment.getValue();
		}
		return null;
	}

	private static final class AtomAssignment<V> {
		private final V value;
		private final int decisionLevel;

		AtomAssignment(V value, int decisionLevel) {
			this.value = value;
			this.decisionLevel = decisionLevel;
		}

		V getValue() {
			return value;
		}

		int getDecisionLevel() {
			return decisionLevel;
		}
	}

	public boolean contains(int literal) {
		final V v = get(atomOf(literal));
		return v != null && isNegated(literal) == v.isNegative();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[ ");
		for (Map.Entry<Integer, AtomAssignment<V>> item : assignment.entrySet()) {
			sb.append(item.getKey());
			sb.append(" := ");
			sb.append(item.getValue().getValue().toString());
			sb.append("(");
			sb.append(item.getValue().getDecisionLevel());
			sb.append("), ");
		}
		sb.append("]");

		return sb.toString();
	}
}
