package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedFunctionTerm extends ParsedTerm {
	public String functionName;
	public int arity;
	public ArrayList<ParsedTerm> termList;
}
