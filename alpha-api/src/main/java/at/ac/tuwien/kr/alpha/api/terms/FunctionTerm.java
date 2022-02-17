package at.ac.tuwien.kr.alpha.api.terms;

import java.util.List;
import java.util.function.Function;

/**
 * A term representing an uninterpreted function
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface FunctionTerm extends Term {

	List<Term> getTerms();

	String getSymbol();

	FunctionTerm renameVariables(Function<String, String> mapping);

}
