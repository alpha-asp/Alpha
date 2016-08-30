package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedFunctionTerm extends ParsedTerm {
	public String functionName;
	public int arity;
	public ArrayList<ParsedTerm> termList;

	@Override
	public String toString() {
		String ret = functionName + "(";
		for (int i = 0; i < arity; i++) {
			ret += (i == 0 ? "" : ", ") + termList.get(i);
		}
		ret += ")";
		return ret;
	}
}
