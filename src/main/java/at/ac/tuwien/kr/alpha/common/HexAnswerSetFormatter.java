package at.ac.tuwien.kr.alpha.common;

import java.util.*;

public class HexAnswerSetFormatter implements AnswerSetFormatter<List<String>> {
	@Override
	public List<String> format(AnswerSet answerSet) {
		Set<Predicate> predicates = answerSet.getPredicates();

		if (predicates.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<String>();
		for (Iterator<Predicate> iterator = predicates.iterator(); iterator.hasNext();) {
			Predicate predicate = iterator.next();
			Set<BasicAtom> instances = answerSet.getPredicateInstances(predicate);

			if (instances == null || instances.isEmpty()) {
				result.add(predicate.getPredicateName());
				continue;
			}

			for (Iterator<BasicAtom> instanceIterator = instances.iterator(); instanceIterator.hasNext();) {
				result.add(instanceIterator.next().toString().replace(" ", "").replace("()", ""));
			}
		}
		return result;
	}
}
