package at.ac.tuwien.kr.alpha.core.common;

import java.util.SortedSet;

import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;

public interface CoreAnswerSet extends Comparable<CoreAnswerSet> {
	SortedSet<CorePredicate> getPredicates();

	SortedSet<CoreAtom> getPredicateInstances(CorePredicate predicate);

	boolean isEmpty();
}
