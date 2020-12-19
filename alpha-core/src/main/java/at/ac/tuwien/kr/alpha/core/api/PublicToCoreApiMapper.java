package at.ac.tuwien.kr.alpha.core.api;

import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;

public class PublicToCoreApiMapper {

	public static CorePredicate mapPredicate(Predicate predicate) {
		return CorePredicate.getInstance(predicate.getName(), predicate.getArity());
	}

	public static java.util.function.Predicate<CorePredicate> mapPredicateFilter(java.util.function.Predicate<Predicate> filter) {
		return (coreApiPredicate) -> {
			return filter.test(CoreToPublicApiMapper.mapPredicate(coreApiPredicate));
		};
	}

}
