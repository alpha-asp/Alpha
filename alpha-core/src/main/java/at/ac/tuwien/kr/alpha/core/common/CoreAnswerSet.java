package at.ac.tuwien.kr.alpha.core.common;

import java.util.SortedSet;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;

public interface CoreAnswerSet extends Comparable<CoreAnswerSet> {
	SortedSet<Predicate> getPredicates();

	SortedSet<Atom> getPredicateInstances(Predicate predicate);

	boolean isEmpty();
}
