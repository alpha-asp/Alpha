package at.ac.tuwien.kr.alpha.api.programs.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

/**
 * A query for ASP atoms matching a set of filter predicates.
 */
public interface AtomQuery extends java.util.function.Predicate<Atom> {

	/**
	 * Adds a new filter to this AtomQuery.
	 * For an atom <code>a(t1, ..., tn)</code>, the term at index <code>termIdx</code> will be tested against the given filter predicate.
	 * 
	 * @param termIdx the index of the term to test
	 * @param filter  the test predicate to use on terms
	 * @return this AtomQuery with the additional filter added
	 */
	public AtomQuery withFilter(int termIdx, java.util.function.Predicate<Term> filter);

	/**
	 * Convenience method - adds a filter to match names of symbolic constants against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	public AtomQuery withConstantEquals(int termIdx, String str);

	/**
	 * Convenience method - adds a filter to match values of constant terms against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	public AtomQuery withStringEquals(int termIdx, String str);

	/**
	 * Convenience method - adds a filter to check for function terms with a given function symbol and arity.
	 * 
	 * @param termIdx
	 * @param funcSymbol
	 * @param funcArity
	 * @return
	 */
	public AtomQuery withFunctionTerm(int termIdx, String funcSymbol, int funcArity);

	/**
	 * Convenience method - adds a filter to check whether a term is equal to a given term.
	 * 
	 * @param termIdx
	 * @param otherTerm
	 * @return
	 */
	public AtomQuery withTermEquals(int termIdx, Term otherTerm);

	/**
	 * Applies this query to an {@link AnswerSet}.
	 * 
	 * @param as
	 * @return
	 */
	public List<Atom> applyTo(AnswerSet as);

}
