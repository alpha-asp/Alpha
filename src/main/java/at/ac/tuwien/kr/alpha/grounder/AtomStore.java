/**
 * Copyright (c) 2016-2017, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * This class stores ground atoms and provides the translation from an (integer) atomId to a (structured) predicate instance.
 * Copyright (c) 2016-2017, the Alpha Team.
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
	public int getAtomId(Atom groundAtom) {
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
			throw oops("Unknown atom ID encountered: " + atomId, e);
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
		StringBuilder ret = new StringBuilder();
		for (Map.Entry<Atom, Integer> entry : predicateInstancesToAtomIds.entrySet()) {
			ret.append(entry.getValue()).append(" <-> ").append(entry.getKey().toString()).append(System.lineSeparator());
		}
		return ret.toString();
	}
}