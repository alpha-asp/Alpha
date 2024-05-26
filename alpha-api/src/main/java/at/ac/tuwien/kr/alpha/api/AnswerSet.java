package at.ac.tuwien.kr.alpha.api;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AtomQuery;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * API representation of an answer set, i.e. a set of atoms that is a model of an ASP program.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface AnswerSet extends Comparable<AnswerSet> {

	/**
	 * The set of all predicates contained in the answer set.
	 */
	SortedSet<Predicate> getPredicates();

	/**
	 * All instances of the given predicate within the answer set.
	 */
	SortedSet<Atom> getPredicateInstances(Predicate predicate);

	/**
	 * Boolean flag indicating whether this {@link AnswerSet} represents the empty set.
	 */
	boolean isEmpty();

	/**
	 * List {@link Atom}s in this answer set satisfying the given {@link AnswerSetQuery}.
	 */
	List<Atom> query(AtomQuery query);

	default Set<Atom> asFacts() {
		return getPredicates().stream()
				.map(this::getPredicateInstances)
				.reduce(new TreeSet<Atom>(), (left, right) -> {
					left.addAll(right);
					return left;
				});
	}

}
