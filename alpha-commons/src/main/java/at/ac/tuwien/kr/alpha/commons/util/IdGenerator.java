package at.ac.tuwien.kr.alpha.commons.util;

/**
 * A generator of (per instance) unique values that can be used for uniquely identifying internal variables, etc.
 */
@FunctionalInterface
public interface IdGenerator<T> {

	/**
	 * Retrieves the next identifier value from the internal sequence of this id generator.
	 */
	T getNextId();

}
