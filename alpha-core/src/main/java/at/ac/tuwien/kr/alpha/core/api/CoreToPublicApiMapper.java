package at.ac.tuwien.kr.alpha.core.api;

import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;

public class CoreToPublicApiMapper {

	public static Predicate mapPredicate(CorePredicate corePredicate) {
		return new PredicateWrapper(corePredicate);
	}
}
