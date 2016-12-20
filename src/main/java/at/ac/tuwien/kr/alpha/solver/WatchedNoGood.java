package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

public final class WatchedNoGood extends NoGood {
	private final int[] pointers;
	private int alpha;

	public WatchedNoGood(NoGood noGood, int a, int b, int alpha) {
		super(noGood);

		this.pointers = new int[] {a, b};
		this.alpha = alpha;
		checkPointers();
	}

	private void checkPointers() {
		if (pointers[0] == pointers[1]) {
			throw new IllegalArgumentException("first two pointers must not point at the same literal");
		}

		for (int i = 0; i < pointers.length; i++) {
			if (pointers[i] < 0) {
				throw new IllegalArgumentException("first two pointers must be non-negative");
			}

			if (pointers[i] >= literals.length) {
				throw new IllegalArgumentException("all pointers must not be within upper bound of nogood size");
			}
		}

		if (alpha >= literals.length) {
			throw new IllegalArgumentException("all pointers must not be within upper bound of nogood size");
		}

		if (alpha < -1) {
			throw new IllegalArgumentException("alpha pointer must not be smaller than -1");
		}
	}

	public int getPointer(int index) {
		return pointers[index];
	}

	public void setPointer(int index, int value) {
		pointers[index] = value;
		checkPointers();
	}

	public int getAlphaPointer() {
		return alpha;
	}

	public void setAlphaPointer(int value) {
		alpha = value;
		checkPointers();
	}

	@Override
	public String toString() {
		return super.toString() + "{ " + pointers[0] + " " + pointers[1] + " " + alpha + " }";
	}
}
