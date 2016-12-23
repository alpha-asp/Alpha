package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.BasicAtom;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * This class stores ground atoms and provides the translation from an (integer) atomId to a (structured) predicate instance.
 * Copyright (c) 2016, the Alpha Team.
 */
public class AtomStore {
	private ArrayList<BasicAtom> atomIdsToInternalBasicAtoms = new ArrayList<>();
	private HashMap<BasicAtom, Integer> predicateInstancesToAtomIds = new HashMap<>();
	private IntIdGenerator atomIdGenerator = new IntIdGenerator();

	private ArrayList<Integer> releasedAtomIds = new ArrayList<>();	// contains atomIds ready to be garbage collected if necessary.

	public AtomStore() {
		// Create atomId for falsum (currently not needed, but it gets atomId 0, which cannot represent a negated literal).
		createAtomId(new BasicAtom(new BasicPredicate("\u22A5", 0), true));
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
	public BasicAtom getBasicAtom(int atomId) {
		try {
			return atomIdsToInternalBasicAtoms.get(atomId);
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("AtomStore: Unknown atomId encountered: " + atomId);
		}
	}

	public ListIterator<BasicAtom> listIterator() {
		return atomIdsToInternalBasicAtoms.listIterator();
	}

	/**
	 * Creates a new atomId representing the given ground atom. Multiple calls with the same parameter result in
	 * the same atomId (duplicates check).
	 * @param groundAtom
	 * @return
	 */
	public int createAtomId(BasicAtom groundAtom) {
		Integer potentialId = predicateInstancesToAtomIds.get(groundAtom);
		if (potentialId == null) {
			int newAtomId = atomIdGenerator.getNextId();
			predicateInstancesToAtomIds.put(groundAtom, newAtomId);
			atomIdsToInternalBasicAtoms.add(newAtomId, groundAtom);
			return newAtomId;
		} else {
			return potentialId;
		}
	}

	public boolean isAtomExisting(BasicAtom groundAtom) {
		return predicateInstancesToAtomIds.containsKey(groundAtom);
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
		for (Map.Entry<BasicAtom, Integer> entry : predicateInstancesToAtomIds.entrySet()) {
			ret += entry.getValue() + " <-> " + entry.getKey().toString() + "\n";
		}
		return ret;
	}
}
