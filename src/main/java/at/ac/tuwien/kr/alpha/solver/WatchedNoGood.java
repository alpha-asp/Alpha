package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;

public final class WatchedNoGood implements ImplicationReasonProvider, Iterable<Integer> {
	private final int[] literals;
	private int alpha;
	private int head;

	public WatchedNoGood(NoGood noGood, int a, int b, int alpha) {
		if (noGood.size() < 3) {
			throw oops("WatchedNoGood should not be used for small NoGoods.");
		}
		literals = new int[noGood.size()];
		checkPointers(a, b, alpha);
		int i = 0;
		for (Integer lit : noGood) {
			literals[i++] = lit;
		}
		this.alpha = alpha;
		head = noGood.hasHead() ? 0 : -1;
		if (b == 0) {
			swap(1, a);
		} else {
			swap(0, a);
			swap(1, b);
		}
	}

	private void checkPointers(int a, int b, int alpha) {
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
				return 0;
			case 1:
				return 1;
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	private void swap(int a, int b) {
		int tmp = literals[a];
		literals[a] = literals[b];
		literals[b] = tmp;
		if (hasHead()) {
			// If WatchedNoGood has a head, ensure the head pointer and alpha watch follow the swap.
			if (head == a) {
				head = b;
			} else if (head == b) {
				head = a;
			}
			if (alpha == a) {
				alpha = b;
			} else if (alpha == b) {
				alpha = a;
			}
		}
	}

	public boolean hasHead() {
		return head != -1;
	}

	public int getHead() {
		return literals[head];
	}

	public int getHeadIndex() {
		return head;
	}

	public void setPointer(int index, int value) {
		if (index != 0 && index != 1) {
			throw new IndexOutOfBoundsException();
		}
		swap(index, value);
	}

	public int getLiteralAtPointer(int index) {
		return literals[index];
	}

	public int getLiteral(int index) {
		return literals[index];
	}

	public int getAlphaPointer() {
		return alpha;
	}

	public void setAlphaPointer(int value) {
		alpha = value;
	}

	public int getLiteralAtAlpha() {
		return literals[alpha];
	}

	public int size() {
		return literals.length;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private int i;

			public boolean hasNext() {
				return literals.length > i;
			}

			public Integer next() {
				return literals[i++];
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (hasHead()) {
			sb.append("*");
		}

		sb.append("{ ");

		int hcount = 0;
		for (int literal : literals) {
			sb.append(isPositive(literal) ? "+" : "-");
			sb.append(atomOf(literal));
			sb.append(hasHead() && head == hcount ? "h" : "");
			sb.append(" ");
			hcount++;
		}

		sb.append("}{");
		sb.append(alpha);
		sb.append("}");
		return sb.toString();
	}

	@Override
	public NoGood getNoGood(int impliedLiteral) {
		// FIXME: this should not be necessary, the GroundConflictNoGoodLearner should be able to work with WatchedNoGood directly.
		return new NoGood(literals.clone());
	}
}
