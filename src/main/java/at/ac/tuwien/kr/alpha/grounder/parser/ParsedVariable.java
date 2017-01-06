package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.common.VariableTerm;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedVariable extends ParsedTerm {
	public String variableName;
	public boolean isAnonymous;

	@Override
	public String toString() {
		return variableName;
	}

	@Override
	public Term toTerm() {
		return isAnonymous ? VariableTerm.getNewAnonymousVariable() : VariableTerm.getInstance(variableName);
	}
}
