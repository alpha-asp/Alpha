package at.ac.tuwien.kr.alpha.common;

import java.util.Set;

public interface AnswerSet {
	Set<Predicate> getPredicates();

	Set<PredicateInstance> getPredicateInstances(Predicate predicate);
}
