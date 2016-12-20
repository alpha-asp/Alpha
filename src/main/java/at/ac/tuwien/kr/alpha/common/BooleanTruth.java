package at.ac.tuwien.kr.alpha.common;

/**
 * Represents a Boolean truth value.
 * Copyright (c) 2016, the Alpha Team.
 */
public interface BooleanTruth {

	boolean isFalse();

	default boolean isTrue() {
		return !isFalse();
	}
}
