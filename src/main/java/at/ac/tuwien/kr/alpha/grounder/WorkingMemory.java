package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class WorkingMemory {
	protected HashMap<Predicate, ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage>> workingMemory = new HashMap<>();
	private HashSet<IndexedInstanceStorage> modifiedWorkingMemories = new HashSet<>();

	public boolean contains(Predicate predicate) {
		return workingMemory.containsKey(predicate);
	}

	public void initialize(Predicate predicate) {
		if (workingMemory.containsKey(predicate)) {
			return;
		}

		IndexedInstanceStorage pos = new IndexedInstanceStorage(predicate, false);
		IndexedInstanceStorage neg = new IndexedInstanceStorage(predicate, true);
		// Index all positions of the storage (may impair efficiency)
		for (int i = 0; i < predicate.getArity(); i++) {
			pos.addIndexPosition(i);
			neg.addIndexPosition(i);
		}

		workingMemory.put(predicate, new ImmutablePair<>(pos, neg));
	}

	public IndexedInstanceStorage get(Literal literal) {
		return get(literal, !literal.isNegated());
	}

	public IndexedInstanceStorage get(Atom atom, boolean value) {
		return get(atom.getPredicate(), value);
	}

	public IndexedInstanceStorage get(Predicate predicate, boolean value) {
		ImmutablePair<IndexedInstanceStorage, IndexedInstanceStorage> pair = workingMemory.get(predicate);
		if (value) {
			return pair.getLeft();
		} else {
			return pair.getRight();
		}
	}

	public void addInstance(Atom atom, boolean value) {
		addInstance(atom.getPredicate(), value, new Instance(atom.getTerms()));
	}

	public void addInstance(Predicate predicate, boolean value, Instance instance) {
		IndexedInstanceStorage storage = get(predicate, value);

		if (!storage.containsInstance(instance)) {
			storage.addInstance(instance);
			modifiedWorkingMemories.add(storage);
		}
	}

	public void addInstances(Predicate predicate, boolean value, Iterable<Instance> instances) {
		IndexedInstanceStorage storage = get(predicate, value);

		for (Instance instance : instances) {
			if (!storage.containsInstance(instance)) {
				storage.addInstance(instance);
				modifiedWorkingMemories.add(storage);
			}
		}
	}

	public void reset() {
		modifiedWorkingMemories = new LinkedHashSet<>();
	}

	public Set<IndexedInstanceStorage> modified() {
		return modifiedWorkingMemories;
	}
}
