/*
 * Copyright (c) 2018-2020, 2022, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.common.atoms;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public class AggregateLiteral extends Literal {
	public AggregateLiteral(AggregateAtom atom, boolean positive) {
		super(atom, positive);
	}

	@Override
	public AggregateAtom getAtom() {
		return (AggregateAtom)atom;
	}

	@Override
	public AggregateLiteral negate() {
		return new AggregateLiteral(getAtom(), !positive);
	}

	/**
	 * @see Atom#substitute(Substitution)
	 */
	@Override
	public AggregateLiteral substitute(Substitution substitution) {
		return new AggregateLiteral(getAtom().substitute(substitution), positive);
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		Set<VariableTerm> bindingVariables = new HashSet<>();
		if (boundBindingVariable(getAtom().getLowerBoundOperator(), getAtom().getLowerBoundTerm(), positive) != null) {
			bindingVariables.add((VariableTerm) getAtom().getLowerBoundTerm());
		}
		if (boundBindingVariable(getAtom().getUpperBoundOperator(), getAtom().getUpperBoundTerm(), positive) != null) {
			bindingVariables.add((VariableTerm) getAtom().getUpperBoundTerm());
		}
		return bindingVariables;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		// Note: every local variable that also occurs globally in the rule is a nonBindingVariable, hence an
		// aggregate literal alone cannot detect its non-binding (i.e. global) variables.
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<VariableTerm> getOccurringVariables() {
		final Set<VariableTerm> variables = new HashSet<>();
		for (AggregateAtom.AggregateElement aggregateElement : getAtom().aggregateElements) {
			variables.addAll(aggregateElement.getOccurringVariables());
		}
		return variables;
	}

	private static VariableTerm boundBindingVariable(ComparisonOperator op, Term bound, boolean positive) {
		boolean isNormalizedEquality = op == ComparisonOperator.EQ && positive || op == ComparisonOperator.NE && !positive;
		if (isNormalizedEquality &&  bound instanceof VariableTerm) {
			return (VariableTerm) bound;
		}
		return null;
	}

}
