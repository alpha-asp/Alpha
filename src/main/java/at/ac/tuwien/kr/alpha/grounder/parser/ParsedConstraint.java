package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedConstraint extends CommonParsedObject {
	public final List<ParsedAtom> body;

	public ParsedConstraint(List<ParsedAtom> body) {
		this.body = body;
	}

	@Override
	public boolean addTo(ParsedProgram program) {
		return program.addConstraint(this);
	}
}
