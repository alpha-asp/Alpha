/**
 * Copyright (c) 2020, the Alpha Team.
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.api.externals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public final class ExternalUtils {

	private ExternalUtils() {

	}

	/**
	 * Convenience method for implementations of external atoms.
	 * Returns a set containing the given value as it's only element.
	 */
	public static <T> Set<T> wrapAsSet(T value) {
		Set<T> retVal = new HashSet<>();
		retVal.add(value);
		return retVal;
	}

	/**
	 * Convenience method for implementation of external atoms.
	 * Takes a single {@link ConstantTerm} and wraps it into a
	 * Set<List<ConstantTerm>>
	 * 
	 * @param <T>  the value type of the term
	 * @param term the term to wrap
	 * @return a Set<List<ConstantTerm<T>>> that contains the input term as it's
	 *         only value
	 */
	public static <T extends Comparable<T>> Set<List<ConstantTerm<T>>> wrapSingleTerm(ConstantTerm<T> term) {
		List<ConstantTerm<T>> termLst = new ArrayList<>();
		termLst.add(term);
		return ExternalUtils.wrapAsSet(termLst);
	}

}
