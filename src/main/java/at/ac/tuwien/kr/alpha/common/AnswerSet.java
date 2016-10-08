package at.ac.tuwien.kr.alpha.common;

import java.util.List;
import java.util.Set;

public interface AnswerSet {
	List<Predicate> getPredicates();

	Set<PredicateInstance> getPredicateInstances(Predicate predicate);
}
