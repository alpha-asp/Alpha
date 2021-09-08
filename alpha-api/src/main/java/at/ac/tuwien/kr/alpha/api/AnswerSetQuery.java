package at.ac.tuwien.kr.alpha.api;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public interface AnswerSetQuery extends java.util.function.Predicate<Atom> {

	public AnswerSetQuery withFilter(int termIdx, java.util.function.Predicate<Term> filter);

	/**
	 * Convenience method - adds a filter to match names of symbolic constants against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	public AnswerSetQuery withConstantEquals(int termIdx, String str);

	/**
	 * Convenience method - adds a filter to match values of constant terms against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	public AnswerSetQuery withStringEquals(int termIdx, String str);

	/**
	 * Convenience method - adds a filter to check for function terms with a given function symbol and arity.
	 * 
	 * @param termIdx
	 * @param funcSymbol
	 * @param funcArity
	 * @return
	 */
	public AnswerSetQuery withFunctionTerm(int termIdx, String funcSymbol, int funcArity);

	/**
	 * Convenience method - adds a filter to check whether a term is equal to a given term.
	 * 
	 * @param termIdx
	 * @param otherTerm
	 * @return
	 */
	public AnswerSetQuery withTermEquals(int termIdx, Term otherTerm);

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
	public boolean test(Atom atom);

	/**
	 * Applies this query to an {@link AnswerSet}.
	 * 
	 * @param as
	 * @return
	 */
	public List<Atom> applyTo(AnswerSet as);	
	
}
