package at.ac.tuwien.kr.alpha.solver;

import java.util.HashSet;

/**
 * An interface to reasons of implications as used internally by the solver. This is a lightweight {@link at.ac.tuwien.kr.alpha.common.NoGood} that only
 * provides an array of literals (in some order) and has an activity that may change.
 *
 * Copyright (c) 2019, the Alpha Team.
 */
public interface Antecedent {


	int[] getReasonLiterals();

	void bumpActivity();

	void decreaseActivity();

	/**
	 * Tests whether two Antecedent objects have the same reason literals (irrespective of their order).
	 * Note that both Antecedents are assumed to contain no duplicate literals.
	 * @param l left Antecedent.
	 * @param r right Antecedent
	 * @return true iff both Antecedents contain the same literals.
	 */
	static boolean antecedentsEquals(Antecedent l, Antecedent r) {
		if (l == r) {
			return true;
		}
		if (l != null && r != null && l.getReasonLiterals().length == r.getReasonLiterals().length) {
			HashSet<Integer> lSet = new HashSet<>();
			for (int literal : l.getReasonLiterals()) {
				lSet.add(literal);
			}
			for (int literal : r.getReasonLiterals()) {
				if (!lSet.contains(literal)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
