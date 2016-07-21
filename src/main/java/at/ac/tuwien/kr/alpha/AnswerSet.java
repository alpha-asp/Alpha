package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;

import java.util.List;
import java.util.Map;

public interface AnswerSet {

	Map<GrounderPredicate, Integer> getPredicateArityMap();

	List<String> getPredicateInstacesAsString(GrounderPredicate predicate, Integer arity);

	List<Integer> getPredicateInstancesAsTermId(GrounderPredicate predicate, Integer arity);

	Map<Integer, String> getTermIdToStringMap();
}
