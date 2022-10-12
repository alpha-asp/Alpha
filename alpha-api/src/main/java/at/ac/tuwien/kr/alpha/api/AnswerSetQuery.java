package at.ac.tuwien.kr.alpha.api;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

/**
 * A {@link java.util.function.Predicate} testing {@link Atom}s in order to query {@link AnswerSet}s for {@link Atom}s satisfying a specific
 * query.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface AnswerSetQuery extends java.util.function.Predicate<Atom> {

	/**
	 * Adds a filter predicate to apply on terms at the given index position.
	 * 
	 * @param termIdx the term index on which to apply the new filter
	 * @param filter  a filter predicate
	 * @return this answer set query withthe given filter added
	 */
	AnswerSetQuery withFilter(int termIdx, java.util.function.Predicate<Term> filter);

	/**
	 * Convenience method - adds a filter to match names of symbolic constants against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	AnswerSetQuery withConstantEquals(int termIdx, String str);

	/**
	 * Convenience method - adds a filter to match values of constant terms against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	AnswerSetQuery withStringEquals(int termIdx, String str);

	/**
	 * Convenience method - adds a filter to check for function terms with a given function symbol and arity.
	 * 
	 * @param termIdx
	 * @param funcSymbol
	 * @param funcArity
	 * @return
	 */
	AnswerSetQuery withFunctionTerm(int termIdx, String funcSymbol, int funcArity);

	/**
	 * Convenience method - adds a filter to check whether a term is equal to a given term.
	 * 
	 * @param termIdx
	 * @param otherTerm
	 * @return
	 */
	AnswerSetQuery withTermEquals(int termIdx, Term otherTerm);

	/**
	 * Applies this query to an atom. Filters are worked off in
	 * order of ascending term index in a conjunctive fashion, i.e. for an atom
	 * to match the query, all of its terms must satisfy all filters on these
	 * terms
	 * 
	 * @param atom the atom to which to apply the query
	 * @return true iff the atom satisfies the query
	 */
	@Override
	boolean test(Atom atom);

	/**
	 * Applies this query to an {@link AnswerSet}.
	 * 
	 * @param as
	 * @return
	 */
	List<Atom> applyTo(AnswerSet as);

}
