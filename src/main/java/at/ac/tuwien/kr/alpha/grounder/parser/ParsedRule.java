package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedRule extends CommonParsedObject{
	public ParsedAtom head;
	public ArrayList<ParsedAtom> body;

	public ParsedRule() {
		body = new ArrayList<>();
	}
}
