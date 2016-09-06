package at.ac.tuwien.kr.alpha.solver.nogood;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.Assignment;

public class WatchedNoGood extends AbstractWatchedNoGood {
	protected int a;
	protected int b;

	public WatchedNoGood(NoGood noGood, int a, int b) {
		super(noGood);
		if (a == b) {
			throw new IllegalArgumentException("a must not be b");
		}

		if (a < 0 || a >= noGood.size()) {
			throw new IllegalArgumentException("a must be non-negative");
		}

		if (b < 0 || b >= noGood.size()) {
			throw new IllegalArgumentException("a must be non-negative");
		}

		this.a = a;
		this.b = b;
	}

	public int getA() {
		return a;
	}

	/**
	 * @throws IllegalArgumentException if {@code a} is negative or equals {@code b}.
	 */
	public void setA(int a) {
		if (a < 0) {
			throw new IllegalArgumentException("a must be non-negative");
		}

		if (a == b) {
			throw new IllegalArgumentException("a must not be b");
		}

		this.a = a;
	}

	public int getB() {
		return b;
	}

	/**
	 * @throws IllegalArgumentException if {@code b} is negative or equals {@code a}.
	 */
	public void setB(int b) {
		if (b < 0) {
			throw new IllegalArgumentException("a must be non-negative");
		}

		if (a == b) {
			throw new IllegalArgumentException("a must not be b");
		}

		this.b = b;
	}

	@Override
	public boolean isUnit(Assignment assignment) {
		return false;
	}
}
