package at.ac.tuwien.kr.alpha.grounder.parser;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedConstant extends ParsedTerm {
	public String content;

	enum TYPE {STRING, NUMBER, CONSTANT}

	public TYPE type;

	@Override
	public String toString() {
		return content;
	}
}
