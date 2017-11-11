/**
 * Copyright (c) 2016-2017, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

public class NoGood implements Iterable<Integer>, Comparable<NoGood> {
	protected final int[] literals;
	private final int head;

	public NoGood(int... literals) {
		this(literals, -1);
	}

	public NoGood(int[] literals, int head) {
		final int headLiteral = head != -1 ? literals[head] : 0;

		// HINT: this might decrease performance if NoGoods are mostly small.
		Arrays.sort(literals);

		// Remove duplicates and find position head was moved to in one pass.
		int headPos = (head != -1 && headLiteral == literals[0]) ? 0 : -1; // check for head literal at position 0.
		int shift = 0;
		for (int i = 1; i < literals.length; i++) {
			if (head != -1 && headPos == -1 && literals[i] == headLiteral) { // check for head literal at position i
				headPos = i - shift;
			}
			if (literals[i - 1] == literals[i]) { // check for duplicate
				shift++;
			}
			literals[i - shift] = literals[i]; // Remove duplicates in place by shifting remaining literals.
		}
		this.head = headPos;

		// copy-shrink array if needed.
		this.literals = shift <= 0 ? literals : Arrays.copyOf(literals, literals.length - shift);
	}

	public NoGood(NoGood noGood) {
		this.literals = noGood.literals.clone();
		this.head = noGood.head;
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

	public boolean isUnary() {
		return size() == 1;
	}

	public boolean isBinary() {
		return size() == 2;
	}

	public int[] getLiteralsClone() {
		return literals.clone();
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

	/**
	 * Analyzes the type of this NoGood and checks if it is the so-called "body, not head" type. Uses the given {@code isRuleBody} interpretation to check whether an
	 * atom represents a rule body.
	 * 
	 * @return {@code true} iff: the NoGood is binary, and it has a head, and its tail is an atom representing a rule body.
	 */
	public boolean isBodyNotHead(Predicate<? super Integer> isRuleBody) {
		return isBinary() && hasHead() && isRuleBody.test(atomOf(getFirstTailLiteral()));
	}

	/**
	 * Returns the first literal that is not pointed to by the head.
	 */
	private int getFirstTailLiteral() {
		return head != 0 ? literals[0] : literals[1];
	}

	/**
	 * Analyzes the type of this NoGood and checks if it is the so-called "body elements, not body" type. Uses the given {@code isRuleBody} interpretation to check
	 * whether an atom represents a rule body.
	 * 
	 * @return {@code true} iff: the NoGood contains at least two literals, and the head is a negative literal whose atom represents a rule body.
	 */
	public boolean isBodyElementsNotBody(Predicate<? super Integer> isRuleBody) {
		return size() > 1 && hasHead() && isNegated(literals[head]) && isRuleBody.test(atomOf(literals[head]));
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
	
	public IntStream stream() {
		return Arrays.stream(literals);
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

		for (int literal : literals) {
			sb.append(literal);
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
}
