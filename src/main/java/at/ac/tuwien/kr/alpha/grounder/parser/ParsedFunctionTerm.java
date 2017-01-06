package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Term;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.common.FunctionTerm.getFunctionTerm;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedFunctionTerm extends ParsedTerm {
	public String functionName;
	public int arity;
	public ArrayList<ParsedTerm> termList;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(functionName).append("(");
		Util.appendDelimited(sb, termList);
		return sb.append(")").toString();
	}

	@Override
	public Term toTerm() {
		return getFunctionTerm(functionName, termList.stream().map(ParsedTerm::toTerm).collect(Collectors.toList()));
	}
}
