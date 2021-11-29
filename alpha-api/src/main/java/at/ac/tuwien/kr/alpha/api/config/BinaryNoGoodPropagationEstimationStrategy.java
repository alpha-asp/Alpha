package at.ac.tuwien.kr.alpha.api.config;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The available strategies for estimating initial heuristic values for heuristics like VSIDS.
 * Modern heuristics determine dynamically which atoms are of high interest. Initial values however must be determined
 * by some other mechanism. One possibility is MOMS where the propagation from binary nogoods is used to estimate how
 * much influence each atom might have.
 */
public enum BinaryNoGoodPropagationEstimationStrategy {
	/**
	 * Estimate influence of literal under consideration by simply counting the number of binary nogoods (i.e., the
	 * length of the binary watch-list) this literal occurs in.
	 * Requires less computation than BinaryNoGoodPropagation but is also less precise.
	 */
	CountBinaryWatches,

	/**
	 * Count how many atoms are assigned after the literal under consideration is assigned true.
	 * This requires an assignment and a propagation cycle followed by a backtracking. More precise than
	 * CountBinaryWatches but significantly more computation required.
	 */
	BinaryNoGoodPropagation;

	/**
	 * @return a comma-separated list of names of known heuristics
	 */
	public static String listAllowedValues() {
		return Arrays.stream(values()).map(BinaryNoGoodPropagationEstimationStrategy::toString).collect(Collectors.joining(", "));
	}
}
