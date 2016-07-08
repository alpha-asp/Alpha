package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedAtom extends CommonParsedObject{
	public String predicate;
	public ArrayList<ParsedTerm> terms;
	public int arity;

	public boolean isNegated;
}
