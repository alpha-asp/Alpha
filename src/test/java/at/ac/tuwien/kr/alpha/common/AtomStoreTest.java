package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class AtomStoreTest {

	public static void fillAtomStore(AtomStore atomStore, int numberOfAtomsToFill) {
		Predicate predA = Predicate.getInstance("a", 1);
		for (int i = 0; i < numberOfAtomsToFill; i++) {
			atomStore.putIfAbsent(new BasicAtom(predA, ConstantTerm.getInstance(i)));
		}
	}
}