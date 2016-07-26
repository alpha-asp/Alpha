package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.Predicate;

import java.util.List;
import java.util.Map;

public interface AnswerSet {

	List<Predicate> getPredicateList();

	List<String> getPredicateInstacesAsString(Predicate predicate);

	List<Integer> getPredicateInstancesAsTermId(Predicate predicate);
	// List<Instance>  where Instance has a pointer to the Predicate and a List<Term>, this requires new Term interface + FunctionalTerm + ConstantTerm

	Map<Integer, String> getTermIdToStringMap();
}
