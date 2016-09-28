package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.Grounder;

import static java.lang.Math.abs;

public class NoGood {
	private final int[] literals;
	private final int head;

	/**
	 * Constructs a NoGood of specified size without a head literal.
	 * @param size
	 */
	public NoGood(int size) {
		this(new int[size], -1);
	}

	public NoGood(int[] literals) {
		this(literals, -1);
	}

	public NoGood(int[] literals, int head) {
		this.literals = literals;
		this.head = head;
	}

	public int size() {
		return literals.length;
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
	public String toString() {
		String ret = "{";
		for (int i = 0; i < literals.length; i++) {
			ret += (i == 0 ? "" : ", ") + literals[i];
		}
		ret += "}[" + head + "]";
		return ret;
	}

	public String toStringReadable(Grounder grounder) {
		String ret = "{";
		for (int i = 0; i < literals.length; i++) {
			ret += (i == 0 ? "" : ", ") + (literals[i] < 0 ? "-" : "+") + grounder.atomIdToString(abs(literals[i]));
		}
		ret += "}[" + head + "]";
		return ret;
	}
}
