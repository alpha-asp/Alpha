package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.common.FunctionTerm.getInstance;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedFunctionTerm extends ParsedTerm {
	private final String functionName;
	private final List<ParsedTerm> termList;

	public ParsedFunctionTerm(String functionName, List<ParsedTerm> termList) {
		this.functionName = functionName;
		this.termList = Collections.unmodifiableList(termList);
	}

	public String getFunctionName() {
		return functionName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(functionName).append("(");
		Util.appendDelimited(sb, termList);
		return sb.append(")").toString();
	}

	@Override
	public Term toTerm() {
		return getInstance(functionName, termList.stream().map(ParsedTerm::toTerm).collect(Collectors.toList()));
	}
}
