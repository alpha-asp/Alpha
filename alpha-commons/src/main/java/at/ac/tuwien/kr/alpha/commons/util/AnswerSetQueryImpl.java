package at.ac.tuwien.kr.alpha.commons.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.AnswerSetQuery;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

/**
 * A query for ASP atoms matching a set of filter predicates.
 */
public final class AnswerSetQueryImpl implements AnswerSetQuery {

	private final Predicate predicate;
	private Map<Integer, java.util.function.Predicate<Term>> filters = new HashMap<>();

	private AnswerSetQueryImpl(Predicate pred) {
		this.predicate = pred;
	}

	/**
	 * Creates a new AnswerSetQuery that will match all atoms that are instances of the given {@link Predicate}.
	 * 
	 * @param predicate the predicate to match against
	 * @return a new AnswerSetQuery matching against the given predicate
	 */
	public static AnswerSetQueryImpl forPredicate(Predicate predicate) {
		return new AnswerSetQueryImpl(predicate);
	}

	/**
	 * Adds a new filter to this AnswerSetQuery.
	 * For an atom <code>a(t1, ..., tn)</code>, the term at index <code>termIdx</code> will be tested against the given filter predicate.
	 * 
	 * @param termIdx the index of the term to test
	 * @param filter  the test predicate to use on terms
	 * @return this AnswerSetQuery with the additional filter added
	 */
	@Override
	public AnswerSetQueryImpl withFilter(int termIdx, java.util.function.Predicate<Term> filter) {
		if (termIdx >= this.predicate.getArity()) {
			throw new IndexOutOfBoundsException(
					"Predicate " + this.predicate.getName() + " has arity " + this.predicate.getArity() + ", term index " + termIdx + " is invalid!");
		}
		if (this.filters.containsKey(termIdx)) {
			java.util.function.Predicate<Term> currFilter = this.filters.get(termIdx);
			this.filters.put(termIdx, currFilter.and(filter));
		} else {
			this.filters.put(termIdx, filter);
		}
		return this;
	}

	/**
	 * Convenience method - adds a filter to match names of symbolic constants against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	@Override
	public AnswerSetQueryImpl withConstantEquals(int termIdx, String str) {
		return this.withFilter(termIdx, AnswerSetQueryImpl.constantTermEquals(str));
	}

	/**
	 * Convenience method - adds a filter to match values of constant terms against a string.
	 * 
	 * @param termIdx
	 * @param str
	 * @return
	 */
	@Override
	public AnswerSetQueryImpl withStringEquals(int termIdx, String str) {
		return this.withFilter(termIdx, (term) -> {
			if (!(term instanceof ConstantTerm<?>)) {
				return false;
			}
			if (((ConstantTerm<?>) term).isSymbolic()) {
				return false;
			}
			return ((ConstantTerm<?>) term).getObject().equals(str);
		});
	}

	/**
	 * Convenience method - adds a filter to check for function terms with a given function symbol and arity.
	 * 
	 * @param termIdx
	 * @param funcSymbol
	 * @param funcArity
	 * @return
	 */
	@Override
	public AnswerSetQueryImpl withFunctionTerm(int termIdx, String funcSymbol, int funcArity) {
		java.util.function.Predicate<Term> isFunction = (term) -> {
			if (!(term instanceof FunctionTerm)) {
				return false;
			}
			FunctionTerm funcTerm = (FunctionTerm) term;
			if (!funcTerm.getSymbol().equals(funcSymbol)) {
				return false;
			}
			if (funcTerm.getTerms().size() != funcArity) {
				return false;
			}
			return true;
		};
		return this.withFilter(termIdx, isFunction);
	}

	/**
	 * Convenience method - adds a filter to check whether a term is equal to a given term.
	 * 
	 * @param termIdx
	 * @param otherTerm
	 * @return
	 */
	@Override
	public AnswerSetQueryImpl withTermEquals(int termIdx, Term otherTerm) {
		java.util.function.Predicate<Term> isEqual = (term) -> {
			return term.equals(otherTerm);
		};
		return this.withFilter(termIdx, isEqual);
	}

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
	public boolean test(Atom atom) {
		if (!atom.getPredicate().equals(predicate)) {
			return false;
		}
		for (int i = 0; i < atom.getTerms().size(); i++) {
			Term ithTerm = atom.getTerms().get(i);
			java.util.function.Predicate<Term> ithFilter = filters.get(i);
			if (ithFilter != null && !ithFilter.test(ithTerm)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Applies this query to an {@link AnswerSet}.
	 * 
	 * @param as
	 * @return
	 */
	@Override
	public List<Atom> applyTo(AnswerSet as) {
		if (!as.getPredicates().contains(this.predicate)) {
			return Collections.emptyList();
		}
		return as.getPredicateInstances(this.predicate).stream().filter(this).collect(Collectors.toList());
	}

	private static java.util.function.Predicate<Term> constantTermEquals(final String str) {
		java.util.function.Predicate<Term> equalsGivenString = (t) -> {
			return AnswerSetQueryImpl.constantTermEquals(t, str);
		};
		return equalsGivenString;
	}

	private static boolean constantTermEquals(Term term, String str) {
		if (!(term instanceof ConstantTerm<?>)) {
			return false;
		}
		if (!((ConstantTerm<?>) term).isSymbolic()) {
			return false;
		}
		return ((ConstantTerm<?>) term).getObject().toString().equals(str);
	}

}
