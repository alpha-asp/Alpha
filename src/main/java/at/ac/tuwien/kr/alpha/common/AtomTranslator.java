package at.ac.tuwien.kr.alpha.common;

import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

@FunctionalInterface
public interface AtomTranslator {
	String atomToString(int atom);

	default String literalToString(int literal) {
		return (isNegated(literal) ? "-" : "+") + "(" + atomToString(atomOf(literal)) + ")";
	}

	/**
	 * Prints the NoGood such that literals are structured atoms instead of integers.
	 * @param noGood the nogood to translate
	 * @return the string representation of the NoGood.
	 */
	default <T extends NoGood> String noGoodToString(T noGood) {
		StringBuilder sb = new StringBuilder();

		if (noGood.hasHead()) {
			sb.append("*");
		}
		sb.append("{");

		for (Iterator<Integer> iterator = noGood.iterator(); iterator.hasNext();) {
			sb.append(literalToString(iterator.next()));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}
}
