package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores directives appearing in the ASP program. Each directive starts with # and ends with .
 * Copyright (c) 2017, the Alpha Team.
 */
public class InlineDirectives {

	public enum DIRECTIVE {
		enum_atom_is
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

	public void accumulate(InlineDirectives other) {
		for (Map.Entry<DIRECTIVE, String> directiveEntry : other.directives.entrySet()) {
			addDirective(directiveEntry.getKey(), directiveEntry.getValue());
		}
	}
}
