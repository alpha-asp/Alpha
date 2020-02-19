package at.ac.tuwien.kr.alpha.api.externals;

import java.util.HashSet;
import java.util.Set;

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

}
