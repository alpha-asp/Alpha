package at.ac.tuwien.kr.alpha.api.programs;

import java.util.Map;

/**
 * Deprecated - This should be refactored: Currently there can only be one instance of each directive, which should not be the case. Also,
 * directives should probably be objects rather than key-value pairs.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
@Deprecated
public interface InlineDirectives {

	public enum DIRECTIVE {
		enum_predicate_is
	}

	void accumulate(InlineDirectives other);

	Map<DIRECTIVE, String> getDirectives();

	void addDirective(DIRECTIVE directive, String text);

	String getDirectiveValue(DIRECTIVE directive);

}
