package at.ac.tuwien.kr.alpha.common;

import java.util.List;

public interface AnswerSet {

	List<String> getPredicateInstancesAsString(Predicate predicate);

	List<PredicateInstance> getPredicateInstances(Predicate predicate);
}
