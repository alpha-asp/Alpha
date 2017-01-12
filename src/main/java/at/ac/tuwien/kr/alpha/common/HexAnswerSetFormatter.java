package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HexAnswerSetFormatter implements AnswerSetFormatter<String[]> {
	@Override
	public String[] format(AnswerSet answerSet) {
		Set<Predicate> predicates = answerSet.getPredicates();

		if (predicates.isEmpty()) {
			return new String[0];
		}

		List<String> result = new ArrayList<String>();
		for (Iterator<Predicate> iterator = predicates.iterator(); iterator.hasNext();) {
			Predicate predicate = iterator.next();
			Set<Atom> instances = answerSet.getPredicateInstances(predicate);

			if (instances == null || instances.isEmpty()) {
				result.add(predicate.getPredicateName());
				continue;
			}

			for (Iterator<Atom> instanceIterator = instances.iterator(); instanceIterator.hasNext();) {
				result.add(instanceIterator.next().toString().replace(" ", "").replace("()", ""));
			}
		}
		return result.toArray(new String[result.size()]);
	}
}
