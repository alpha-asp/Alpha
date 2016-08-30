package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Term;

import java.util.*;

/**
 * A storage for instances with a certain arity, where each position of the instance can be indexed.
 * This aids in matching and joining instances. An index can be added or removed at any time for a desired position of
 * all instances.
 * Copyright (c) 2016, the Alpha Team.
 */
public class IndexedInstanceStorage {

	private final String description;	// An (arbitrary) description of what is stored here, mainly for debugging.
	private final int arity;		// All instances stored have to have this number of termIds.
	private HashSet<Instance> instances;	// A collection of all instances currently stored in this storage.
	private ArrayList<HashMap<Term, ArrayList<Instance>>> indices;	// For each position, a mapping of termIds to list of instances with this termId at the corresponding position

	public IndexedInstanceStorage(String description, int arity) {
		this.description = description;
		this.arity = arity;
		instances = new HashSet<>();
		// Create list of mappings, initialize to null.
		indices = new ArrayList<>();
		while (indices.size() < arity) {
			indices.add(null);
		}
	}

	public void addIndexPosition(int position) {
		if (position < 0 || position > arity - 1) {
			throw new RuntimeException("Requested to create indices for attribute out of range." +
				"IndexedInstanceStorage: " + description + " arity: " + arity + "  requested indices position: " + position);
		}
		// Add index
		indices.set(position, new HashMap<>());

		// Initialize index with all instances currently used.
		for (Instance instance : instances) {
			indices.get(position).putIfAbsent(instance.terms[position], new ArrayList<>());
			ArrayList<Instance> instancesAtPosition = indices.get(position).get(instance.terms[position]);
			instancesAtPosition.add(instance);
		}
	}

	public void removeIndexPosition(int position) {
		if (position < 0 || position > arity - 1) {
			throw new RuntimeException("Requested to create indices for attribute out of range." +
				"IndexedInstanceStorage: " + description + " arity: " + arity + "  requested indices position: " + position);
		}
		indices.set(position, null);
	}

	public void addInstance(Instance instance) {
		if (instance.terms.length != arity) {
			throw new RuntimeException("Instance length does not match arity of IndexedInstanceStorage: " +
				"instance size: " + instance.terms.length
				+ "IndexedInstanceStorage size: " + arity);
		}
		instances.add(instance);
		// Add instance to all indices.
		for (int i = 0; i < indices.size(); i++) {
			HashMap<Term, ArrayList<Instance>> posIndex = indices.get(i);
			if (posIndex == null) {
				continue;
			}
			posIndex.putIfAbsent(instance.terms[i], new ArrayList<>());
			ArrayList<Instance> matchingInstancesAtPos = posIndex.get(instance.terms[i]);
			matchingInstancesAtPos.add(instance);	// Add instance
		}
	}

	public void removeInstance(Instance instance) {
		// Remove from all indices
		for (int i = 0; i < indices.size(); i++) {
			HashMap<Term, ArrayList<Instance>> posIndex = indices.get(i);
			if (posIndex == null) {
				continue;
			}
			ArrayList<Instance> matchingInstancesAtPos = posIndex.get(instance.terms[i]);
			matchingInstancesAtPos.remove(instance);	// Remove instance

			// If there are no more instances having the term at the current position,
			// remove the entry from the hash.
			if (matchingInstancesAtPos.size() == 0) {
				posIndex.remove(instance.terms[i]);
			}
		}
		instances.remove(instance);
	}

	/**
	 * Returns a list of all instances having the given term at the given position. Returns null if no such
	 * instances exist.
	 * @param term
	 * @param position
	 * @return
	 */
	public List<Instance> getInstancesMatchingAtPosition(Term term, int position) {
		HashMap<Term, ArrayList<Instance>> indexForPosition = indices.get(position);
		if (indexForPosition == null) {
			throw new RuntimeException("IndexedInstanceStorage queried for position " + position + " which is not indexed.");
		}
		return indexForPosition.get(term);
	}

	public Set<Instance> getAllInstances() {
		return instances;
	}

}
