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

		if (c == getHead()) {
			throw new IllegalArgumentException("c cannot point at head");
		}

		this.c = c;
	}

	public int getPointer(int index) {
		switch (index) {
			case 0: return getA();
			case 1: return getB();
			case 2: return getC();
			default: throw new IndexOutOfBoundsException();
		}
	}

	public void setPointer(int index, int value) {
		switch (index) {
			case 0: setA(value);
			case 1: setB(value);
			case 2: setC(value);
			default: throw new IndexOutOfBoundsException();
		}
	}
}
