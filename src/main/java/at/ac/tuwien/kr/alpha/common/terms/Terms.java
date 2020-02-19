package at.ac.tuwien.kr.alpha.common.terms;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods for {@link Term}s.
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

	@SafeVarargs
	public static <T extends Comparable<T>> List<ConstantTerm<String>> asSymbolicTermList(T... values) {
		List<ConstantTerm<String>> retVal = new ArrayList<>();
		for (T value : values) {
			retVal.add(ConstantTerm.getSymbolicInstance(value.toString()));
		}
		return retVal;
	}

}
