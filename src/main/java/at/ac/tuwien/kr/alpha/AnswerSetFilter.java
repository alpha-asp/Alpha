package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public interface AnswerSetFilter {

	boolean isPredicateFiltered(GrounderPredicate predicate);
}
