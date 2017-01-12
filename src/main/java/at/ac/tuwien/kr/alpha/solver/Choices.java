package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.ReadableAssignment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class Choices implements Iterable<Map.Entry<Integer, Pair<Integer, Integer>>> {
	private final Map<Integer, Pair<Integer, Integer>> choices;

	public Choices(Map<Integer, Pair<Integer, Integer>> choices) {
		this.choices = choices;
	}

	public Choices() {
		this(new HashMap<>());
	}

	public boolean isActive(int atom, ReadableAssignment<ThriceTruth> assignment) {
		Pair<Integer, Integer> pair = choices.get(atom);

		if (pair == null) {
			return false;
		}

		ThriceTruth truth = assignment.getTruth(pair.getLeft());

		// Check if choice point is enabled.
		if (!TRUE.equals(truth) && !MBT.equals(truth)) {
			return false;
		}

		// Ensure it is not disabled.
		truth = assignment.getTruth(pair.getRight());
		return truth == null || FALSE.equals(truth);
	}

	public void put(int atom, int enabler, int disabler) {
		choices.put(atom, new ImmutablePair<>(enabler, disabler));
	}

	public void putAll(Choices other) {
		choices.putAll(other.choices);
	}

	public Iterator<Map.Entry<Integer, Pair<Integer, Integer>>> iterator() {
		return choices.entrySet().iterator();
	}

	public boolean is(int atom) {
		return choices.containsKey(atom);
	}
}
