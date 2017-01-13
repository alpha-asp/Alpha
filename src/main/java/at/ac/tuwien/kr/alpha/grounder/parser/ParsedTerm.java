package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class ParsedTerm extends CommonParsedObject {
	public abstract Term toTerm();
}
