package at.ac.tuwien.kr.alpha.solver;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Choices implements Iterable<Map.Entry<Integer, Pair<Integer, Integer>>> {
	private final Map<Integer, Pair<Integer, Integer>> choices;

	public Choices(Map<Integer, Pair<Integer, Integer>> choices) {
		this.choices = choices;
	}

	public Choices() {
		this(new HashMap<>());
	}

	public Set<Integer> getAtoms() {
		return choices.keySet();
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
