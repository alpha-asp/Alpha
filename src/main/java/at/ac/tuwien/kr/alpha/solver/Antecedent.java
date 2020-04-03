/*
 * Copyright (c) 2019-2020, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;

import java.util.Iterator;

/**
 * An interface to reasons of implications as used internally by the solver. This is a lightweight {@link at.ac.tuwien.kr.alpha.common.NoGood} that only
 * provides an array of literals (in some order) and has an activity that may change.
 *
 * Copyright (c) 2019-2020, the Alpha Team.
 */
public interface Antecedent extends Iterable<Integer> {

	int[] getReasonLiterals();

	void bumpActivity();

	void decreaseActivity();

	/**
	 * Gets the {@link NoGood} from which this Antecedent has been created
	 * @return the original nogood, if known, else {@code null}.
	 */
	default NoGood getOriginalNoGood() {
		return null;
	}

	@Override
	default Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private int i;
			private int[] literals = getReasonLiterals();

			@Override
			public boolean hasNext() {
				return literals.length > i;
			}

			@Override
			public Integer next() {
				return literals[i++];
			}
		};
	}

	default int size() {
		return getReasonLiterals().length;
	}
}
