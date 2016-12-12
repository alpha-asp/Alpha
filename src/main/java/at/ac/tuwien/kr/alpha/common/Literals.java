package at.ac.tuwien.kr.alpha.common;

import static java.lang.Math.abs;

public final class Literals {
	/**
	 * Given a literal, returns the corresponding atom.
	 * @param literal the literal to translate.
	 * @return the corresponding atom.
	 */
	public static int atomOf(int literal) {
		if (literal == 0) {
			throw new IllegalArgumentException("Zero is not a literal (because it cannot be negated).");
		}
		return abs(literal);
	}

	/**
	 * A utility to check if a given literal is negated.
	 * @param literal the literal to check.
	 * @return {@code true} iff the literal is negated, {@code false} otherwise.
	 */
	public static boolean isNegated(int literal) {
		return literal < 0;
	}

	public static boolean isPositive(int literal) {
		return literal > 0;
	}
}
