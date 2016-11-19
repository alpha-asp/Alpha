package at.ac.tuwien.kr.alpha.common;

import java.util.Set;

public interface AnswerSet {
	Set<Predicate> getPredicates();

	Set<BasicAtom> getPredicateInstances(Predicate predicate);

	boolean isEmpty();
}
