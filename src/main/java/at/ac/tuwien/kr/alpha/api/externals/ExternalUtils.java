package at.ac.tuwien.kr.alpha.api.externals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public final class ExternalUtils {

	private ExternalUtils() {

	}

	/**
	 * Convenience method for implementations of external atoms.
	 * Returns a set containing the given value as it's only element.
	 */
	public static <T> Set<T> wrapAsSet(T value) {
		Set<T> retVal = new HashSet<>();
		retVal.add(value);
		return retVal;
	}

	/**
	 * Convenience method for implementation of external atoms.
	 * Takes a single {@link ConstantTerm} and wraps it into a
	 * Set<List<ConstantTerm>>
	 * 
	 * @param <T>  the value type of the term
	 * @param term the term to wrap
	 * @return a Set<List<ConstantTerm<T>>> that contains the input term as it's
	 *         only value
	 */
	public static <T extends Comparable<T>> Set<List<ConstantTerm<T>>> wrapSingleTerm(ConstantTerm<T> term) {
		List<ConstantTerm<T>> termLst = new ArrayList<>();
		termLst.add(term);
		return ExternalUtils.wrapAsSet(termLst);
	}

}
