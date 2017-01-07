package at.ac.tuwien.kr.alpha.grounder.parser;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class CommonParsedObject {
	public boolean addTo(ParsedProgram program) {
		throw new UnsupportedOperationException("Unknown parsed object encountered during program parsing: " + this);
	}
}
