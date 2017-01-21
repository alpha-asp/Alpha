package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.common.VariableTerm;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedVariable extends ParsedTerm {
	public static final ParsedVariable ANONYMOUS = new ParsedVariable(null, true);

	private final String variableName;
	private final boolean isAnonymous;

	protected ParsedVariable(String variableName, boolean isAnonymous) {
		this.variableName = variableName;
		this.isAnonymous = isAnonymous;
	}

	public ParsedVariable(String variableName) {
		this(variableName, false);
	}

	@Override
	public String toString() {
		return variableName;
	}

	@Override
	public Term toTerm() {
		return isAnonymous ? VariableTerm.getAnonymousInstance() : VariableTerm.getInstance(variableName);
	}
}
