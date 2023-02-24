package at.ac.tuwien.kr.alpha.api.config;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The available reboot strategies.
 */
public enum RebootStrategy {
	FIXED,
	GEOM,
	LUBY,
	ASSIGN,
	ANSWER;

	/**
	 * @return a comma-separated list of names of known reboot strategies
	 */
	public static String listAllowedValues() {
		return Arrays.stream(values()).map(RebootStrategy::toString).collect(Collectors.joining(", "));
	}
}
