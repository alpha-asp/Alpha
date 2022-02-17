package at.ac.tuwien.kr.alpha.api.rules.heads;

import java.util.function.Function;

/**
 * The head of an ASP rule.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Head {

	Head renameVariables(Function<String, String> mapping);
	
}
