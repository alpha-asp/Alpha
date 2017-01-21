package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.Util;

import java.util.SortedSet;

public interface AnswerSet extends Comparable<AnswerSet> {
	SortedSet<Predicate> getPredicates();

	SortedSet<Atom> getPredicateInstances(Predicate predicate);

	boolean isEmpty();

	default int compareTo(AnswerSet other) {
		final SortedSet<Predicate> predicates = this.getPredicates();
		int result = Util.compareSortedSets(predicates, other.getPredicates());

		if (result != 0) {
			return result;
		}

		for (Predicate predicate : predicates) {
			result = Util.compareSortedSets(this.getPredicateInstances(predicate), other.getPredicateInstances(predicate));

			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}
