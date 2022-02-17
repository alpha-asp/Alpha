package at.ac.tuwien.kr.alpha.api.terms;

import java.util.function.Function;

/**
 * A term consisting of a constant symbol.
 * 
 * @param <T> the type of object represented by this term.
 *            Copyright (c) 2021, the Alpha Team.
 */
public interface ConstantTerm<T extends Comparable<T>> extends Term {

	T getObject();

	boolean isSymbolic();

	default ConstantTerm<T> renameVariables(Function<String, String> mapping) {
		// A constant is by definition variable-free, just return the constant.
		return this;
	}

}
