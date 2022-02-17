package at.ac.tuwien.kr.alpha.api.terms;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;

/**
 * Common super-interface for terms supported by Alpha.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface Term extends Comparable<Term> {

	boolean isGround();

	Set<VariableTerm> getOccurringVariables();

	Term substitute(Substitution substitution);

	Term renameVariables(Function<String, String> mapping);

	Term normalizeVariables(String renamePrefix, RenameCounter counter);

	public static interface RenameCounter {

		Map<VariableTerm, VariableTerm> getRenamedVariables();

		int getAndIncrement();

	}

}
