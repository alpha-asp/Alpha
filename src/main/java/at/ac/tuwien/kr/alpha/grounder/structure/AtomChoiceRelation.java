package at.ac.tuwien.kr.alpha.grounder.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and provides relationships between ordinary atoms and those that represent choice points.
 *
 * Copyright (c) 2019, the Alpha Team.
 */
public class AtomChoiceRelation {
	private final ArrayList<ArrayList<Integer>> atomToChoiceAtoms = new ArrayList<>();

	public void addRelation(int atom, int bodyRepresentingAtom) {
		while (atom > atomToChoiceAtoms.size() - 1) {
			atomToChoiceAtoms.add(new ArrayList<>());
		}
		atomToChoiceAtoms.get(atom).add(bodyRepresentingAtom);
	}

	public List<Integer> getRelatedChoiceAtoms(int atom) {
		return Collections.unmodifiableList(atomToChoiceAtoms.get(atom));
	}

	public void growForMaxAtomId(int maxAtomId) {
		while (maxAtomId > atomToChoiceAtoms.size() - 1) {
			atomToChoiceAtoms.add(new ArrayList<>());
		}
	}
}
