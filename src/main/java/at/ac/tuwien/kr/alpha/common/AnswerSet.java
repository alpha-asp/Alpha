package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;

public interface AnswerSet extends Comparable<AnswerSet> {
	SortedSet<Predicate> getPredicates();

	SortedSet<Atom> getPredicateInstances(Predicate predicate);

	boolean isEmpty();

	@Override
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

	/**
	 * Gives a "flat" version of this answer set by transforming it into a list of
	 * atoms
	 */
	default List<Atom> flatten() {
		List<Atom> flatAnswerSet = new ArrayList<>();
		for (Predicate p : this.getPredicates()) {
			for (Atom a : this.getPredicateInstances(p)) {
				flatAnswerSet.add(a);
			}
		}
		return flatAnswerSet;
	}
}
