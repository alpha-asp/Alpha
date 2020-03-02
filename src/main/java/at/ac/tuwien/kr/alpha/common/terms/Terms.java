package at.ac.tuwien.kr.alpha.common.terms;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods for {@link Term}s. The methods provided here are an
 * attempt to avoid repeating commonly used code snippets, like wrapping sets of
 * values in {@link Term}s and creating lists of those terms, etc.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public final class Terms {

	private Terms() {

	}

	@SafeVarargs
	public static <T extends Comparable<T>> List<ConstantTerm<T>> asTermList(T... values) {
		List<ConstantTerm<T>> retVal = new ArrayList<>();
		for (T value : values) {
			retVal.add(ConstantTerm.getInstance(value));
		}
		return retVal;
	}

}
