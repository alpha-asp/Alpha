package at.ac.tuwien.kr.alpha.api;

import java.util.SortedSet;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;

public interface AnswerSet extends Comparable<AnswerSet> {
	
	SortedSet<Predicate> getPredicates();

	SortedSet<Atom> getPredicateInstances(Predicate predicate);

	boolean isEmpty();
}
