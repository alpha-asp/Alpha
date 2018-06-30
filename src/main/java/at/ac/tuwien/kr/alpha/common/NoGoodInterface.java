package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.solver.ImplicationReasonProvider;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public interface NoGoodInterface extends ImplicationReasonProvider {

	/**
	 * Returns the literal at the given index.
	 * @param index the index position within the NoGood.
	 * @return the literal at the index.
	 */
	int getLiteral(int index);

	/**
	 * Returns whether the NoGood has a head.
	 * @return true if the NoGood has a head.
	 */
	boolean hasHead();

	/**
	 * Returns the head literal of the NoGood, if present.
	 * @return the head literal if the NoGood has a head, otherwise an arbitrary integer.
	 */
	int getHead();

	/**
	 * Returns the size, i.e., number of literals, in the NoGood.
	 * @return the size of the NoGood.
	 */
	int size();

	default boolean isUnary() {
		return size() == 1;
	}

	default boolean isBinary() {
		return size() == 2;
	}
}
