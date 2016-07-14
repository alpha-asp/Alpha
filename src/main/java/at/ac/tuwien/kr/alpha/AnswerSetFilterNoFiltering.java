package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class AnswerSetFilterNoFiltering implements AnswerSetFilter {
	@Override
	public boolean isPredicateFiltered(GrounderPredicate predicate) {
		return false;
	}
}
