package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.Term;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedConstant extends ParsedTerm {
	public String content;

	@Override
	public Term toTerm() {
		return ConstantTerm.getInstance(content);
	}

	enum TYPE {STRING, NUMBER, CONSTANT}

	public TYPE type;

	@Override
	public String toString() {
		return content;
	}
}
