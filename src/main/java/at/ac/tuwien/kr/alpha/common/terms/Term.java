package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

/**
 * Common representation of Terms. Terms are constructed such that each term is represented by a unique object, hence
 * term equality can be checked by object reference comparison. Each concrete subclass of a Term must implement a
 * factory-like method to obtain instances.
 *
 * We use {@link Comparable} to establish the following ordering among terms:
 * <ol>
 *         <li>
 *                 Constant terms according to their corresponding object and its type
 *                 <ol>
 *                         <li>{@link ConstantTerm<Integer>} ordered by value of the integers</li>
 *                         <li>{@link ConstantTerm<at.ac.tuwien.kr.alpha.common.Symbol>} lexicographically ordered on the symbol</li>
 *                         <li>{@link ConstantTerm<String>} lexicographicall
 * First, all constant terms with integers
 * ordered according to the values of the integers, then all constant terms with symbols, according to the lexicographic
 * order of their symbols, then all constant terms with strings, according to the lexicographic order of their strings,
 * then all constant terms with other objects, where the ordering of comparison is the ordering imposed by
 * {@link Comparable} for terms of the same type, and the lexicograSymbolphical ordering of the type names for constants of
 * different types. Then all function symbols follow, ordered by arity, functor name, and ordering based on the
 * arguments.
 * Variable terms are last.y ordered on the string</li>
 *                         <li>{@link ConstantTerm} for all other types, where {@link Comparable#compareTo(Object)} is
 *                         used as ordering whenever possible (i.e. two terms' objects have the same type). For two
 *                         terms with objects of different type, the result is the lexicographic ordering of the type
 *                         names.</li>
 *                 </ol>
 *         </li>
 *         <li>Function terms (ordered by arity, functor name, and then on their argument terms).</li>
 *         <li>Variable terms (lexicographically ordered on their variable names)</li>
 * </ol>
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public abstract class Term implements Comparable<Term> {
	public abstract boolean isGround();

	public abstract List<VariableTerm> getOccurringVariables();

	/**
	 * Applies a substitution, result may be nonground.
	 * @param substitution the variable substitution to apply.
	 * @return the non-substitute term where all variable substitutions have been applied.
	 */
	public abstract Term substitute(Substitution substitution);

	private static int priority(Term term) {
		final Class<?> clazz = term.getClass();
		if (clazz.equals(ConstantTerm.class)) {
			return 1;
		} else if (clazz.equals(FunctionTerm.class)) {
			return 2;
		} else if (clazz.equals(VariableTerm.class)) {
			return 3;
		}
		throw new UnsupportedOperationException("Can only compare constant term, function terms and variable terms among each other.");
	}

	@Override
	public int compareTo(Term o) {
		return o == null ? 1 : Integer.compare(priority(this), priority(o));
	}
}
