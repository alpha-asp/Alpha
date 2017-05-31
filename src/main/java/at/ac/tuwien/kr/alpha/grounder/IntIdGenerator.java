package at.ac.tuwien.kr.alpha.grounder;

/**
 * Generates unique, sequential integers starting at 0, i.e., it maintains a counter that is incremented for each getNextId().
 * Copyright (c) 2016, the Alpha Team.
 */
public class IntIdGenerator {
	private int highestId;

	public IntIdGenerator() {
		this(0);
	}

	public IntIdGenerator(int initial) {
		this.highestId = initial;
	}

	public int getNextId() {
		if (highestId == Integer.MAX_VALUE) {
			throw new RuntimeException("Ran out of Ids (integer overflow).");
		}
		return highestId++;
	}

	/**
	 * Resets the internal counter. Useful for resetting before each test.
	 */
	public void resetGenerator() {
		highestId = 0;
	}
}
