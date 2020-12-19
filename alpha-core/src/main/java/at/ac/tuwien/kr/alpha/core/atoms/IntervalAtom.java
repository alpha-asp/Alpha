/**
 * Copyright (c) 2017-2018, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.core.atoms;

import static at.ac.tuwien.kr.alpha.core.util.Util.join;
import static at.ac.tuwien.kr.alpha.core.util.Util.oops;

import java.util.Arrays;
import java.util.List;

import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.core.grounder.Substitution;

/**
 * Helper for treating IntervalTerms in rules.
 *
 * Each IntervalTerm is replaced by a variable and this special IntervalAtom is added to the rule body for generating
 * all bindings of the variable.
 *
 * The first term of this atom is an IntervalTerm while the second term is any Term (if it is a VariableTerm, this will
 * bind to all elements of the interval, otherwise it is a simple check whether the Term is a ConstantTerm<Integer> with
 * the Integer being inside the interval.
 * 
 * Copyright (c) 2017, the Alpha Team.
 */
public class IntervalAtom extends CoreAtom implements VariableNormalizableAtom {
	private static final CorePredicate PREDICATE = CorePredicate.getInstance("_interval", 2, true);

	private final List<CoreTerm> terms;

	public IntervalAtom(IntervalTerm intervalTerm, CoreTerm intervalRepresentingVariable) {
		this.terms = Arrays.asList(intervalTerm, intervalRepresentingVariable);
	}

	@Override
	public CorePredicate getPredicate() {
		return PREDICATE;
	}

	@Override
	public List<CoreTerm> getTerms() {
		return terms;
	}

	@Override
	public boolean isGround() {
		for (CoreTerm t : this.terms) {
			if (!t.isGround()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IntervalLiteral toLiteral(boolean positive) {
		if (!positive) {
			throw oops("IntervalLiteral cannot be negated");
		}
		return new IntervalLiteral(this);
	}

	@Override
	public IntervalLiteral toLiteral() {
		return toLiteral(true);
	}

	@Override
	public String toString() {
		return join(PREDICATE.getName() + "(", terms, ")");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		IntervalAtom that = (IntervalAtom) o;

		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return terms.hashCode();
	}

	@Override
	public IntervalAtom substitute(Substitution substitution) {
		return new IntervalAtom((IntervalTerm) terms.get(0).substitute(substitution), terms.get(1).substitute(substitution));
	}

	@Override
	public IntervalAtom normalizeVariables(String prefix, int counterStartingValue) {
		List<CoreTerm> renamedTerms = CoreTerm.renameTerms(terms, prefix, counterStartingValue);
		return new IntervalAtom((IntervalTerm) renamedTerms.get(0), renamedTerms.get(1));
	}

	@Override
	public CoreAtom withTerms(List<CoreTerm> terms) {
		throw new UnsupportedOperationException("IntervalAtoms do not support setting of terms!");
	}
}
