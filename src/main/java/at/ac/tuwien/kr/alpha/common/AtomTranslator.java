/**
 * Copyright (c) 2016-2018, the Alpha Team.
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

import java.util.Iterator;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

@FunctionalInterface
public interface AtomTranslator {
	String atomToString(int atom);

	default String literalToString(int literal) {
		return (isNegated(literal) ? "-" : "+") + "(" + atomToString(atomOf(literal)) + ")";
	}

	default String literalsToString(Iterable<Integer> literals) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		for (Iterator<Integer> iterator = literals.iterator(); iterator.hasNext();) {
			sb.append(literalToString(iterator.next()));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");
		return sb.toString();
	}

	/**
	 * Prints the NoGood such that literals are structured atoms instead of integers.
	 * @param noGood the nogood to translate
	 * @return the string representation of the NoGood.
	 */
	default <T extends NoGood> String noGoodToString(T noGood) {
		StringBuilder sb = new StringBuilder();

		if (noGood.hasHead()) {
			sb.append("*");
		}
		sb.append(literalsToString(noGood));

		return sb.toString();
	}
}
