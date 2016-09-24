package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

public class ConflictingNoGoodException extends RuntimeException {
	private final NoGood noGood;

	public ConflictingNoGoodException(NoGood noGood) {
		super("Encountered conflicting NoGood " + noGood);
		this.noGood = noGood;
	}

	public NoGood getConflictingNoGood() {
		return noGood;
	}
}
