package at.ac.tuwien.kr.alpha.common;

import java.util.Arrays;
import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

public class NoGood implements Iterable<Integer>, Comparable<NoGood> {
	private final int[] literals;
	private final int head;

	public NoGood(int... literals) {
		this(literals, -1);
	}

	public NoGood(int[] literals, int head) {
		if (!isSorted(literals)) {
			throw new IllegalArgumentException("Literals are not sorted");
		}

		this.literals = literals;
		this.head = head;
	}

	private static boolean isSorted(int[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			if (a[i] > a[i + 1]) {
				return false;
			}
		}
		return true;
	}

	public int size() {
		return literals.length;
	}

	public int getAtom(int index) {
		return atomOf(getLiteral(index));
	}

	public int getLiteral(int index) {
		return literals[index];
	}

	public void setLiteral(int index, int value) {
		literals[index] = value;
	}

	public int getHead() {
		return head;
	}

	public boolean hasHead() {
		return head >= 0;
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
	public int compareTo(NoGood o) {
		if (o == null) {
			throw new NullPointerException("Cannot compare against null.");
		}

		if (o.literals.length > literals.length) {
			return -1;
		}

		if (o.literals.length < literals.length) {
			return +1;
		}

		for (int i = 0; i < literals.length; i++) {
			if (o.literals[i] > literals[i]) {
				return -1;
			}
			if (o.literals[i] < literals[i]) {
				return +1;
			}
		}

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		NoGood noGood = (NoGood) o;

		return head == noGood.head && Arrays.equals(literals, noGood.literals);
	}

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(literals) + head;
	}

	public static NoGood headFirst(int... literals) {
		return new NoGood(literals, 0);
	}

	public static NoGood fact(int literal) {
		return headFirst(literal);
	}
}
