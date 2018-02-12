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
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;

/**
 * Represents a builtin atom according to the standard.
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class ComparisonLiteral extends Literal implements FixedInterpretationLiteral {
	private final boolean isNormalizedEquality;

	public ComparisonLiteral(Term left, Term right, boolean negated, ComparisonOperator operator) {
		this(Arrays.asList(left, right), negated, operator);
	}

	private ComparisonLiteral(List<Term> terms, boolean negated, ComparisonOperator operator) {
		this(new ComparisonAtom(terms, operator), negated);
	}

	private ComparisonLiteral(ComparisonAtom atom, boolean negated) {
		super(atom, negated);
		this.isNormalizedEquality = (!negated && atom.operator == ComparisonOperator.EQ)
				|| (negated && atom.operator == ComparisonOperator.NE);
	}

	public boolean isNormalizedEquality() {
		return isNormalizedEquality;
	}

	boolean isLeftAssigning() {
		return isNormalizedEquality && getAtom().isLeftAssigning();
	}

	boolean isRightAssigning() {
		return isNormalizedEquality && getAtom().isRightAssigning();
	}

	@Override
	public ComparisonAtom getAtom() {
		return (ComparisonAtom) atom;
	}

	@Override
	public ComparisonLiteral negate() {
		return new ComparisonLiteral(getAtom(), !negated);
	}

	@Override
	public Predicate getPredicate() {
		return atom.getPredicate();
	}

	@Override
	public List<Term> getTerms() {
		return atom.getTerms();
	}

	@Override
	public boolean isGround() {
		return atom.isGround();
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		if (isLeftAssigning() && isRightAssigning()) {
			// In case this is "X = Y" or "not X != Y" then both sides are binding given that the other is.
			// In this case non-binding and binding variables cannot be reported accurately, in fact, the double variable could be compiled away.
			throw new RuntimeException("Builtin equality with left and right side being variables encountered. Should not happen.");
		}
		if (isLeftAssigning()) {
			return Collections.singleton((VariableTerm) getTerms().get(0));
		}
		if (isRightAssigning()) {
			return Collections.singleton((VariableTerm) getTerms().get(1));
		}
		return Collections.emptySet();
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		Term left = getTerms().get(0);
		Term right = getTerms().get(1);
		HashSet<VariableTerm> occurringVariables = new HashSet<>();
		List<VariableTerm> leftOccurringVariables = new LinkedList<>(left.getOccurringVariables());
		List<VariableTerm> rightOccurringVariables = new LinkedList<>(right.getOccurringVariables());
		if (isLeftAssigning()) {
			leftOccurringVariables.remove(left);
		}
		if (isRightAssigning()) {
			rightOccurringVariables.remove(right);
		}
		occurringVariables.addAll(leftOccurringVariables);
		occurringVariables.addAll(rightOccurringVariables);
		if (isLeftAssigning() || isRightAssigning()) {
			return occurringVariables;
		}
		// Neither left- nor right-assigning, hence no variable is binding.
		return occurringVariables;
	}

	@Override
	public ComparisonLiteral substitute(Substitution substitution) {
		return new ComparisonLiteral(getAtom().substitute(substitution), negated);
	}

	@Override
	public List<Substitution> getSubstitutions(Substitution partialSubstitution) {
		ComparisonAtom atom = getAtom();
		ComparisonOperator operator = negated ? atom.operator.getNegation() : atom.operator;
		return ComparisonAtom.getSubstitutions(atom, partialSubstitution, operator);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ComparisonLiteral that = (ComparisonLiteral) o;

		if (negated != that.negated) {
			return false;
		}
		if (getAtom().operator != that.getAtom().operator) {
			return false;
		}
		return getTerms().equals(that.getTerms());
	}

	@Override
	public int hashCode() {
		return atom.hashCode() + (negated ? 1 : 0);
	}
}
