package at.ac.tuwien.kr.alpha.core.common.terms;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods for {@link CoreTerm}s. The methods provided here are an
 * attempt to avoid repeating commonly used code snippets, like wrapping sets of
 * values in {@link CoreTerm}s and creating lists of those terms, etc.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public final class Terms {

	/**
	 * Since this is purely a utility class, it may not be instantiated.
	 * 
	 * @throws AssertionError if called
	 */
	private Terms() {
		throw new AssertionError(Terms.class.getSimpleName() + " is a non-instantiable utility class!");
	}

	@SafeVarargs
	public static <T extends Comparable<T>> List<CoreConstantTerm<T>> asTermList(T... values) {
		List<CoreConstantTerm<T>> retVal = new ArrayList<>();
		for (T value : values) {
			retVal.add(CoreConstantTerm.getInstance(value));
		}
		return retVal;
	}

}
