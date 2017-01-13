package at.ac.tuwien.kr.alpha.grounder.parser;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedFact extends CommonParsedObject {
	private final ParsedAtom fact;

	public ParsedFact(ParsedAtom fact) {
		this.fact = fact;
	}

	public ParsedAtom getFact() {
		return fact;
	}

	@Override
	public boolean addTo(ParsedProgram program) {
		return program.addFact(this);
	}
}
