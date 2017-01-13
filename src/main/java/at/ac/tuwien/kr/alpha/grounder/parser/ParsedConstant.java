package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedConstant extends ParsedTerm {
	enum Type { STRING, NUMBER, CONSTANT }

	private final String content;

	private final Type type;
	public ParsedConstant(String content, Type type) {
		this.content = content;
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	@Override
	public Term toTerm() {
		return ConstantTerm.getInstance(content);
	}

	@Override
	public String toString() {
		return content;
	}
}
