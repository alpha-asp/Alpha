package at.ac.tuwien.kr.alpha.api.terms;

import java.util.function.Function;

/**
 * A term representing a variable symbol in ASP programs.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface VariableTerm extends Term {

	String getName();

	VariableTerm renameVariables(Function<String, String> mapping);

}
