package at.ac.tuwien.kr.alpha.common;

public final class Literals {
	/**
	 * Given a literal, returns the corresponding atom.
	 * @param literal the literal to translate.
	 * @return the corresponding atom.
	 */
	public static int atomOf(int literal) {
		return literal >> 1;
	}

	/**
	 * A utility to check if a given literal is negated.
	 * @param literal the literal to check.
	 * @return {@code true} iff the literal is negated, {@code false} otherwise.
	 */
	public static boolean isNegated(int literal) {
		return (literal & 0x1) == 1;
	}

	public static boolean isPositive(int literal) {
		return (literal & 0x1) == 0;
	}

	public static int negateLiteral(int literal) {
		return literal ^ 0x1;
	}

	public static int atomToLiteral(int atom) {
		return atom << 1;
	}

	public static int atomToNegatedLiteral(int atom) {
		return negateLiteral(atomToLiteral(atom));
	}

	public static int positiveLiteral(int literal) {
		return literal & ~0x1;
	}
}
