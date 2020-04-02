/**
 * Copyright (c) 2016-2020, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.lang.test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * An assertion error that may be a result of running unit tests, as specified
 * in the respective
 * <a href="https://github.com/alpha-asp/Alpha/issues/237">github issue</a>.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class AssertionError {

	private final String violatedConstraint;
	private final Set<List<Term>> groundInstances;

	public AssertionError(String violatedConstraint, Set<List<Term>> groundInstances) {
		this.violatedConstraint = violatedConstraint;
		this.groundInstances = Collections.unmodifiableSet(new TreeSet<>(groundInstances));
	}

	public String getViolatedConstraint() {
		return this.violatedConstraint;
	}

	public Set<List<Term>> getGroundInstances() {
		return this.groundInstances;
	}

}