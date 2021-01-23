package at.ac.tuwien.kr.alpha.core.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.program.InlineDirectives;

/**
 * Stores directives appearing in the ASP program. Each directive starts with # and ends with .
 * Copyright (c) 2017 - 2021, the Alpha Team.
 */
public class InlineDirectivesImpl implements InlineDirectives {

	public enum DIRECTIVE {
		enum_predicate_is
	}

	private final LinkedHashMap<DIRECTIVE, String> directives = new LinkedHashMap<>();

	public String getDirectiveValue(DIRECTIVE directive) {
		return directives.get(directive);
	}

	public void addDirective(DIRECTIVE directive, String value) {
		if (directives.get(directive) != null) {
			throw new RuntimeException("Inline directive multiply defined.");
		}
		directives.put(directive, value);
	}

	public void accumulate(InlineDirectivesImpl other) {
		for (Map.Entry<DIRECTIVE, String> directiveEntry : other.directives.entrySet()) {
			addDirective(directiveEntry.getKey(), directiveEntry.getValue());
		}
	}
}
