package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Arrays;
import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static java.lang.Math.abs;

public class NoGood implements Iterable<Integer>, Comparable<NoGood> {
	protected final int[] literals;
	private final int head;

	public NoGood(int... literals) {
		this(literals, -1);
	}

	public NoGood(int[] literals, int head) {
		int headLiteral = head != -1 ? literals[head] : 0;
		Arrays.sort(literals);	// HINT: this might decrease performance if NoGoods are mostly small.

		// Remove duplicates and find position head was moved to in one pass.
		int headPos = (head != -1 && headLiteral == literals[0]) ? 0 : -1;	// check for head literal at position 0.
		int shift = 0;
		for (int i = 1; i < literals.length; i++) {
			if (head != -1 && headPos == -1 && literals[i] == headLiteral) {	// check for head literal at position i
				headPos = i - shift;
			}
			if (literals[i - 1] == literals[i]) {	// check for duplicate
				shift++;
			}
			literals[i - shift] = literals[i];	// Remove duplicates in place by shifting remaining literals.
		}
		this.head = headPos;
		if (shift > 0) {
			this.literals = Arrays.copyOf(literals, literals.length - shift);	// copy-shrink array
		} else  {
			this.literals = literals;
		}
	}

	public NoGood(NoGood noGood) {
		this.literals = noGood.literals.clone();
		this.head = noGood.head;
	}

	private static boolean isSortedAndDistinct(int[] a) {
		for (int i = 0; i < a.length - 1; i++) {
			// Note that checking a[i] >= a[i + 1] is not sufficient
			// as we are looking for duplicate _atoms_ not _literals_.
			if (a[i] > a[i + 1] || atomOf(a[i]) == atomOf(a[i + 1])) {
				return false;
			}
		}
		return true;
	}

	public static NoGood headFirst(int... literals) {
		return new NoGood(literals, 0);
	}

	public static NoGood fact(int literal) {
		return headFirst(literal);
	}

	public int size() {
		return literals.length;
	}

	/**
	 * A shorthand for <code>Literals.atomOf(getLiteral(...))</code>
	 */
	public int getAtom(int index) {
		return atomOf(getLiteral(index));
	}

	public int getLiteral(int index) {
		return literals[index];
	}

	/**
	 * Returns the index of the head literal, if present.
	 * @throws IllegalStateException if there is no head.
	 * @return the index of the head literal.
	 */
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{ ");

		for (int i = 0; i < literals.length; i++) {
			sb.append(literals[i]);
			sb.append(" ");
		}

		sb.append("}");

		if (head == -1) {
			return sb.toString();
		}

		sb.append("[");
		sb.append(head);
		sb.append("]");

		return sb.toString();
	}

	/**
	 * Prints the NoGood such that literals are structured atoms instead of integers.
	 * @param grounder the grounder used for resolving atomIds
	 * @return the string representation of the NoGood.
	 */
	public String toStringReadable(Grounder grounder) {
		String ret = "{";
		for (int i = 0; i < literals.length; i++) {
			ret += (i == 0 ? "" : ", ") + (literals[i] < 0 ? "-" : "+") + grounder.atomIdToString(abs(literals[i]));
		}
		ret += "}[" + head + "]";
		return ret;
	}
}
