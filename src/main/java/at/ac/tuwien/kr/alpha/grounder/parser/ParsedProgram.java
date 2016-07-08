package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedProgram extends CommonParsedObject {
	public ArrayList<ParsedRule> rules;
	public ArrayList<ParsedFact> facts;
	public ArrayList<ParsedConstraint> constraints;

	public ParsedProgram() {
		rules = new ArrayList<>();
		facts = new ArrayList<>();
		constraints = new ArrayList<>();
	}
}
