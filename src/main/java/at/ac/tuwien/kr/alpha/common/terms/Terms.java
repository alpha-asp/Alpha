package at.ac.tuwien.kr.alpha.common.terms;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm.ArithmeticOperator;

/**
 * Convenience methods for {@link Term}s. The methods provided here are an
 * attempt to avoid repeating commonly used code snippets, like wrapping sets of
 * values in {@link Term}s and creating lists of those terms, etc.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public final class Terms {

	/**
	 * Since this is purely a utility class, it may not be instantiated.
	 * 
	 * @throws AssertionError if called
	 */
	private Terms() {
		throw new AssertionError(Terms.class.getSimpleName() + " is a non-instantiable utility class!");
	}

	@SafeVarargs
	public static <T extends Comparable<T>> List<ConstantTerm<T>> asTermList(T... values) {
		List<ConstantTerm<T>> retVal = new ArrayList<>();
		for (T value : values) {
			retVal.add(ConstantTerm.getInstance(value));
		}
		return retVal;
	}

	/**
	 * Creates a new {@link Literal} that maps the given target variable to the given term minus one.
	 * 
	 * @param term
	 * @param targetVariable
	 * @return
	 */
	public static Literal decrementTerm(Term term, VariableTerm targetVariable) {
		Term decrement = ArithmeticTerm.getInstance(term, ArithmeticOperator.MINUS, ConstantTerm.getInstance(1));
		ComparisonAtom atom = new ComparisonAtom(targetVariable, decrement, ComparisonOperator.EQ);
		return atom.toLiteral();
	}

	/**
	 * Creates a new {@link Literal} that maps the given target variable to the given term plus one.
	 * 
	 * @param term
	 * @param targetVariable
	 * @return
	 */
	public static Literal incrementTerm(Term term, VariableTerm targetVariable) {
		Term increment = ArithmeticTerm.getInstance(term, ArithmeticOperator.PLUS, ConstantTerm.getInstance(1));
		ComparisonAtom atom = new ComparisonAtom(targetVariable, increment, ComparisonOperator.EQ);
		return atom.toLiteral();
	}

}
