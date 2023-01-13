package at.ac.tuwien.kr.alpha.api;

import java.util.List;
import java.util.SortedSet;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AtomQuery;

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
}
