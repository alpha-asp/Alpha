package at.ac.tuwien.kr.alpha;

import static java.lang.Math.abs;

public final class Literals {
	public static int atomOf(int literal) {
		return abs(literal);
	}

	public static boolean isNegated(int literal) {
		return literal < 0;
	}
}
