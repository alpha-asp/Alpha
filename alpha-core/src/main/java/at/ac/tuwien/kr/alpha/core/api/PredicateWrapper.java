package at.ac.tuwien.kr.alpha.core.api;

import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;

public class PredicateWrapper implements Predicate {

	private CorePredicate wrapped;

	public PredicateWrapper(CorePredicate corePredicate) {
		this.wrapped = corePredicate;
	}

	@Override
	public int compareTo(Predicate o) {
		return wrapped.compareTo(PublicToCoreApiMapper.mapPredicate(o));
	}

	@Override
	public String getName() {
		return wrapped.getName();
	}

	@Override
	public int getArity() {
		return wrapped.getArity();
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

}
