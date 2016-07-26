package at.ac.tuwien.kr.alpha.solver.nogood;

import at.ac.tuwien.kr.alpha.common.NoGood;

public class WatchedMBTNoGood extends WatchedNoGood {
	private int c;

	public WatchedMBTNoGood(NoGood noGood, int a, int b, int c) {
		super(noGood, a, b);

		if (c < 0) {
			throw new IllegalArgumentException("c must be non-negative");
		}

		this.c = c;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		if (c < 0) {
			throw new IllegalArgumentException("c must be non-negative");
		}

		this.c = c;
	}
}
