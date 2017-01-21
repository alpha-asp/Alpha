package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Atom;
import at.ac.tuwien.kr.alpha.common.BasicAtom;

import java.util.*;

/**
 * This class stores ground atoms and provides the translation from an (integer) atomId to a (structured) predicate instance.
 * Copyright (c) 2016, the Alpha Team.
 */
public class AtomStore {
	private List<Atom> atomIdsToInternalBasicAtoms = new ArrayList<>();
	private Map<Atom, Integer> predicateInstancesToAtomIds = new HashMap<>();
	private IntIdGenerator atomIdGenerator = new IntIdGenerator(1);

	private List<Integer> releasedAtomIds = new ArrayList<>();	// contains atomIds ready to be garbage collected if necessary.

	public AtomStore() {
		// Create atomId for falsum (currently not needed, but it gets atomId 0, which cannot represent a negated literal).
		atomIdsToInternalBasicAtoms.add(null);
	}

	public int getHighestAtomId() {
		return atomIdsToInternalBasicAtoms.size() - 1;
	}

	/**
	 * Returns the AtomId associated with a given ground predicate instance (=ground atom).
	 * @param groundAtom
	 * @return
	 */
	public int getAtomId(BasicAtom groundAtom) {
		return predicateInstancesToAtomIds.get(groundAtom);
	}

	/**
	 * Returns the structured ground atom associated with the given atomId.
	 * @param atomId
	 * @return
	 */
	public Atom get(int atomId) {
		try {
			return atomIdsToInternalBasicAtoms.get(atomId);
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("AtomStore: Unknown atomId encountered: " + atomId, e);
		}
	}

	/**
	 * Creates a new atomId representing the given ground atom. Multiple calls with the same parameter result in
	 * the same atomId (duplicates check).
	 * @param groundAtom
	 * @return
	 */
	public int add(Atom groundAtom) {
		if (!groundAtom.isGround()) {
			throw new IllegalArgumentException("atom must be ground");
		}

		Integer id = predicateInstancesToAtomIds.get(groundAtom);

		if (id == null) {
			id = atomIdGenerator.getNextId();
			predicateInstancesToAtomIds.put(groundAtom, id);
			atomIdsToInternalBasicAtoms.add(id, groundAtom);
		}

		return id;
	}

	public boolean contains(Atom groundAtom) {
		return predicateInstancesToAtomIds.containsKey(groundAtom);
	}

	public ListIterator<Atom> listIterator() {
		return atomIdsToInternalBasicAtoms.listIterator();
	}

	/**
	 * Removes the given atom from the AtomStore.
	 * @param atomId
	 */
	public void releaseAtomId(int atomId) {
		releasedAtomIds.add(atomId);
		// HINT: Additionally removing the terms used in the instance might be beneficial in some cases.
	}

	public String printAtomIdTermMapping() {
		String ret = "";
		for (Map.Entry<Atom, Integer> entry : predicateInstancesToAtomIds.entrySet()) {
			ret += entry.getValue() + " <-> " + entry.getKey().toString() + "\n";
		}
		return ret;
	}
}