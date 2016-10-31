package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.BasicAtom;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores ground atoms and provides the translation from an (integer) atomId to a (structured) predicate instance.
 * Copyright (c) 2016, the Alpha Team.
 */
public class AtomStore {
	private ArrayList<BasicAtom> atomIdsToInternalBasicAtoms = new ArrayList<>();
	private HashMap<BasicAtom, AtomId> predicateInstancesToAtomIds = new HashMap<>();
	private IntIdGenerator atomIdGenerator = new IntIdGenerator();

	private ArrayList<AtomId> releasedAtomIds = new ArrayList<>();	// contains atomIds ready to be garbage collected if necessary.

	public AtomStore() {
		// Create atomId for falsum (currently not needed, but it gets atomId 0, which cannot represent a negated literal).
		createAtomId(new BasicAtom(new BasicPredicate("\u22A5", 0), new Term[0]));
	}

	public AtomId getHighestAtomId() {
		return new AtomId(atomIdsToInternalBasicAtoms.size() - 1);
	}

	/**
	 * Returns the AtomId associated with a given ground predicate instance (=ground atom).
	 * @param groundAtom
	 * @return
	 */
	public AtomId getAtomId(BasicAtom groundAtom) {
		return predicateInstancesToAtomIds.get(groundAtom);
	}

	/**
	 * Returns the structured ground atom associated with the given atomId.
	 * @param atomId
	 * @return
	 */
	public BasicAtom getBasicAtom(AtomId atomId) {
		try {
			return atomIdsToInternalBasicAtoms.get(atomId.atomId);
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("AtomStore: Unknown atomId encountered: " + atomId.atomId);
		}
	}

	/**
	 * Creates a new atomId representing the given ground atom. Multiple calls with the same parameter result in
	 * the same atomId (duplicates check).
	 * @param groundAtom
	 * @return
	 */
	public AtomId createAtomId(BasicAtom groundAtom) {
		AtomId potentialId = predicateInstancesToAtomIds.get(groundAtom);
		if (potentialId == null) {
			AtomId newAtomId = new AtomId(atomIdGenerator.getNextId());
			predicateInstancesToAtomIds.put(groundAtom, newAtomId);
			atomIdsToInternalBasicAtoms.add(newAtomId.atomId, groundAtom);
			return newAtomId;
		} else {
			return potentialId;
		}
	}

	public boolean isAtomExisting(BasicAtom groundAtom) {
		AtomId potentialId = predicateInstancesToAtomIds.get(groundAtom);
		return potentialId != null;
	}

	/**
	 * Removes the given atom from the AtomStore.
	 * @param atomId
	 */
	public void releaseAtomId(AtomId atomId) {
		releasedAtomIds.add(atomId);
		// HINT: Additionally removing the terms used in the instance might be beneficial in some cases.
	}

	public String printAtomIdTermMapping() {
		String ret = "";
		for (Map.Entry<BasicAtom, AtomId> entry : predicateInstancesToAtomIds.entrySet()) {
			ret += entry.getValue().atomId + " <-> " + entry.getKey().toString() + "\n";
		}
		return ret;
	}
}
