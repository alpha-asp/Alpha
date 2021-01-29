package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
// TODO (what is this? where is this used?)
public class AtomStoreTest {

	public static void fillAtomStore(AtomStore atomStore, int numberOfAtomsToFill) {
		Predicate predA = CorePredicate.getInstance("a", 1);
		for (int i = 0; i < numberOfAtomsToFill; i++) {
			atomStore.putIfAbsent(new BasicAtom(predA, CoreConstantTerm.getInstance(i)));
		}
	}
}