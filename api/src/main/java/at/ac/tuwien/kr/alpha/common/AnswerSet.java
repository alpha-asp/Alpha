package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

import java.util.SortedSet;

public interface AnswerSet extends Comparable<AnswerSet> {
	SortedSet<? extends Predicate> getPredicates();

	SortedSet<Atom> getPredicateInstances(Predicate predicate);

	boolean isEmpty();
}
