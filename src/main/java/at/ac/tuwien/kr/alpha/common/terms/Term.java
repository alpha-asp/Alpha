package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.common.Symbol;
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
 *                         <li>{@link ConstantTerm<Symbol>} lexicographically ordered on the symbol</li>
 *                         <li>{@link ConstantTerm<String>} lexicographically ordered on the string</li>
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
public interface Term extends Comparable<Term> {
	boolean isGround();

	List<VariableTerm> getOccurringVariables();

	/**
	 * Applies a substitution, result may be nonground.
	 * @param substitution the variable substitution to apply.
	 * @return the non-substitute term where all variable substitutions have been applied.
	 */
	Term substitute(Substitution substitution);

	@Override
	String toString();
}
