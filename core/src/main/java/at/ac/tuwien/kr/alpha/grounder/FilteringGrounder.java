package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.PredicateImpl;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class FilteringGrounder implements Grounder {
	protected final java.util.function.Predicate<PredicateImpl> filter;

	protected FilteringGrounder(java.util.function.Predicate<PredicateImpl> filter) {
		this.filter = filter;
	}

	protected FilteringGrounder() {
		this(p -> true);
	}
}
