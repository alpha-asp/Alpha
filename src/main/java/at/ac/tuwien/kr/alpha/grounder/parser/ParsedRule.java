package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedRule extends CommonParsedObject{
	public final ParsedAtom head;
	public final List<ParsedAtom> body;

	public ParsedRule(List<ParsedAtom> body, ParsedAtom head) {
		this.head = head;
		this.body = body;
	}

	public ParsedRule(List<ParsedAtom> body) {
		this(body, null);
	}

	@Override
	public boolean addTo(ParsedProgram program) {
		return program.addRule(this);
	}
}
