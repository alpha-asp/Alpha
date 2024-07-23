package at.ac.tuwien.kr.alpha.api.programs.terms;

import java.util.List;

/**
 * A term representing an uninterpreted function
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface FunctionTerm extends Term {

	List<Term> getTerms();

	String getSymbol();

}
