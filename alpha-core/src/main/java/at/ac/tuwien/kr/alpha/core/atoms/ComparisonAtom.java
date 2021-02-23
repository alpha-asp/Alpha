/**
 * Copyright (c) 2017-2020, the Alpha Team.
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.ComparisonOperatorImpl;

/**
 * Represents a builtin comparison atom according to the standard.
 */
public class ComparisonAtom extends CoreAtom implements VariableNormalizableAtom {
	private final Predicate predicate;
	final ComparisonOperatorImpl operator;
	private final List<Term> terms;

	private ComparisonAtom(List<Term> terms, ComparisonOperatorImpl operator) {
		this.terms = terms;
		this.operator = operator;
		this.predicate = operator.predicate();
	}

	public ComparisonAtom(Term term1, Term term2, ComparisonOperatorImpl operator) {
		this(Arrays.asList(term1, term2), operator);
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public List<Term> getTerms() {
		return terms;
	}

	@Override
	public boolean isGround() {
		return terms.get(0).isGround() && terms.get(1).isGround();
	}

	@Override
	public ComparisonAtom substitute(Substitution substitution) {
		List<Term> substitutedTerms = getTerms().stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		return new ComparisonAtom(substitutedTerms, operator);
	}

	@Override
	public ComparisonLiteral toLiteral(boolean positive) {
		return new ComparisonLiteral(this, positive);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(terms.get(0));
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		sb.append(terms.get(1));
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ComparisonAtom that = (ComparisonAtom) o;

		if (operator != that.operator) {
			return false;
		}
		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * (31 * operator.hashCode() + terms.hashCode());
	}

	@Override
	public ComparisonAtom normalizeVariables(String prefix, int counterStartingValue) {
		List<Term> renamedTerms = Terms.renameTerms(terms, prefix, counterStartingValue);
		return new ComparisonAtom(renamedTerms.get(0), renamedTerms.get(1), operator);
	}

	@Override
	public Atom withTerms(List<Term> terms) {
		return new ComparisonAtom(terms, operator);
	}

}
