package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.Arrays;

public final class WatchedNoGood extends NoGood {
	private int a;
	private int b;
	private int alpha;

	public WatchedNoGood(NoGood noGood, int a, int b, int alpha) {
		super(noGood);

		this.a = a;
		this.b = b;
		this.alpha = alpha;
		checkPointers();
	}

	private void checkPointers() {
		if (a == b) {
			throw new IllegalArgumentException("First two pointers must not point at the same literal.");
		}

		if (a < 0 || b < 0 || alpha < -1 || a >= literals.length || b >= literals.length || alpha >= literals.length) {
			throw new IllegalArgumentException("Pointers must be within bounds.");
		}
	}

	public int getPointer(int index) {
		switch (index) {
			case 0:
				return a;
			case 1:
				return b;
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	public void setPointer(int index, int value) {
		switch (index) {
			case 0:
				a = value;
				break;
			case 1:
				b = value;
				break;
			default:
				throw new IndexOutOfBoundsException();
		}
		checkPointers();
	}

	public int getLiteralAtPointer(int index) {
		return getLiteral(getPointer(index));
	}

	public int getAlphaPointer() {
		return alpha;
	}

	public void setAlphaPointer(int value) {
		alpha = value;
		checkPointers();
	}

	public int getLiteralAtAlpha() {
		return getLiteral(getAlphaPointer());
	}

	@Override
	public String toString() {
		return super.toString() + "{ " + a + " " + b + " " + alpha + " }";
	}
}
