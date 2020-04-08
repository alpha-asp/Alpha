/*
 * Copyright (c) 2017, 2018, 2020, the Alpha Team.
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

package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.common.Interner;
import at.ac.tuwien.kr.alpha.common.Substitution;

import java.util.LinkedList;
import java.util.List;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * An IntervalTerm represents the shorthand notation for a set of rules where all elements in this interval occur once, e.g., fact(2..5).
 * An IntervalTerm is a meta-term and the grounder must replace it with its corresponding set of facts or rules.
 */
public class IntervalTerm extends Term {
	private static final Interner<IntervalTerm> INTERNER = new Interner<>();
	private final Term lowerBoundTerm;
	private final Term upperBoundTerm;

	private final int lowerBound;
	private final int upperBound;

	private final boolean ground;

	private IntervalTerm(Term lowerBound, Term upperBound) {
		if (lowerBound == null || upperBound == null) {
			throw new IllegalArgumentException();
		}

		this.ground = !((lowerBound instanceof VariableTerm) || (upperBound instanceof VariableTerm));

		this.lowerBoundTerm = lowerBound;
		this.upperBoundTerm = upperBound;

		if (this.ground) {
			this.upperBound = Integer.parseInt(upperBoundTerm.toString());
			this.lowerBound = Integer.parseInt(lowerBoundTerm.toString());
		} else {
			this.upperBound = -1;
			this.lowerBound = -1;
		}
	}

	public static IntervalTerm getInstance(Term lowerBound, Term upperBound) {
		return INTERNER.intern(new IntervalTerm(lowerBound, upperBound));
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}

	public int getLowerBound() {
		if (!isGround()) {
			throw oops("Cannot get the lower bound of non-ground interval");
		}
		return this.lowerBound;
	}

	public int getUpperBound() {
		if (!isGround()) {
			throw oops("Cannot get the upper bound of non-ground interval");
		}
		return this.upperBound;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> variables = new LinkedList<>();
		if (lowerBoundTerm instanceof VariableTerm) {
			variables.add((VariableTerm) lowerBoundTerm);
		}
		if (upperBoundTerm instanceof VariableTerm) {
			variables.add((VariableTerm) upperBoundTerm);
		}
		return variables;
	}

	@Override
	public IntervalTerm substitute(Substitution substitution) {
		if (isGround()) {
			return this;
		}
		return new IntervalTerm(lowerBoundTerm.substitute(substitution), upperBoundTerm.substitute(substitution));
	}

	@Override
	public String toString() {
		return lowerBoundTerm + ".." + upperBoundTerm;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		IntervalTerm that = (IntervalTerm) o;

		if (!lowerBoundTerm.equals(that.lowerBoundTerm)) {
			return false;
		}
		return upperBoundTerm.equals(that.upperBoundTerm);

	}

	@Override
	public int hashCode() {
		return 31 * lowerBoundTerm.hashCode() + upperBoundTerm.hashCode();
	}

	@Override
	public int compareTo(Term o) {
		if (this == o) {
			return 0;
		}

		if (!(o instanceof IntervalTerm)) {
			return super.compareTo(o);
		}

		IntervalTerm other = (IntervalTerm)o;

		int result = lowerBoundTerm.compareTo(other.lowerBoundTerm);

		if (result != 0) {
			return result;
		}

		return upperBoundTerm.compareTo(other.upperBoundTerm);
	}

	@Override
	public Term renameVariables(String renamePrefix) {
		return new IntervalTerm(lowerBoundTerm.renameVariables(renamePrefix), upperBoundTerm.renameVariables(renamePrefix));
	}

	@Override
	public Term normalizeVariables(String renamePrefix, RenameCounter counter) {
		return IntervalTerm.getInstance(
			lowerBoundTerm.normalizeVariables(renamePrefix, counter),
			upperBoundTerm.normalizeVariables(renamePrefix, counter));
	}

	/**
	 * Returns true if the term contains (or is) some IntervalTerm.
	 * @param term the term to test
	 * @return true iff an IntervalTerm occurs in term.
	 */
	public static boolean termContainsIntervalTerm(Term term) {
		if (term instanceof IntervalTerm) {
			return true;
		} else if (term instanceof FunctionTerm) {
			return functionTermContainsIntervals((FunctionTerm) term);
		} else {
			return false;
		}
	}

	public static boolean functionTermContainsIntervals(FunctionTerm functionTerm) {
		// Test whether a function term contains an interval term (recursively).
		for (Term term : functionTerm.getTerms()) {
			if (term instanceof IntervalTerm) {
				return true;
			}
			if (term instanceof FunctionTerm && functionTermContainsIntervals((FunctionTerm) term)) {
				return true;
			}
		}
		return false;
	}
}