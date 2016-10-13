package at.ac.tuwien.kr.alpha;

import static java.lang.Math.abs;

public final class Literals {
	public static int atomOf(int literal) {
		if (literal == 0) {
			throw new IllegalArgumentException("Zero is not a literal (because it cannot be negated).");
		}
		return abs(literal);
	}

	public static boolean isNegated(int literal) {
		return literal < 0;
	}
}
