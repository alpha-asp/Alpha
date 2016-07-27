package at.ac.tuwien.kr.alpha.common;

import java.util.List;
import java.util.Map;

public interface AnswerSet {

	List<Predicate> getPredicateList();

	List<String> getPredicateInstacesAsString(Predicate predicate);

	List<PredicateInstance> getPredicateInstances(Predicate predicate);

	Map<Integer, String> getTermIdToStringMap();
}
