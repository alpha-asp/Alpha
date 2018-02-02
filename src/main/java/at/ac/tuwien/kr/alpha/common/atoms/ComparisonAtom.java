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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;

/**
 * Represents a builtin atom according to the standard.
 * Copyright (c) 2017, the Alpha Team.
 */
public class ComparisonAtom implements FixedInterpretationLiteral {
	private final Predicate predicate;
	private final ComparisonOperator operator;
	private final List<Term> terms;
	private final boolean negated;
	private final boolean isNormalizedEquality;

	public ComparisonAtom(Term left, Term right, boolean negated, ComparisonOperator operator) {
		this.terms = Arrays.asList(left, right);
		this.negated = negated;
		this.operator = operator;
		this.isNormalizedEquality = (!negated && operator == ComparisonOperator.EQ)
			|| (negated && operator == ComparisonOperator.NE);
		this.predicate = Predicate.getInstance(operator.toString(), 2);
	}

	public boolean isNormalizedEquality() {
		return isNormalizedEquality;
	}

	private boolean isLeftAssigning() {
		return isNormalizedEquality && terms.get(0) instanceof VariableTerm;
	}

	private boolean isRightAssigning() {
		return isNormalizedEquality && terms.get(1) instanceof VariableTerm;
	}

	@Override
	public boolean isNegated() {
		return negated;
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
	public List<VariableTerm> getBindingVariables() {
		if (isLeftAssigning() && isRightAssigning()) {
			// In case this is "X = Y" or "not X != Y" then both sides are binding given that the other is.
			// In this case non-binding and binding variables cannot be reported accurately, in fact, the double variable could be compiled away.
			throw new RuntimeException("Builtin equality with left and right side being variables encountered. Should not happen.");
		}
		if (isLeftAssigning()) {
			return Collections.singletonList((VariableTerm)terms.get(0));
		}
		if (isRightAssigning()) {
			return Collections.singletonList((VariableTerm)terms.get(1));
		}
		return Collections.emptyList();
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		Term left = terms.get(0);
		Term right = terms.get(1);
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
			return new ArrayList<>(occurringVariables);
		}
		// Neither left- nor right-assigning, hence no variable is binding.
		return new ArrayList<>(occurringVariables);
	}

	@Override
	public ComparisonAtom substitute(Substitution substitution) {
		return new ComparisonAtom(terms.get(0).substitute(substitution),
			terms.get(1).substitute(substitution),
			negated, operator);
	}

	@Override
	public List<Substitution> getSubstitutions(Substitution partialSubstitution) {
		// Treat case where this is just comparison with all variables bound by partialSubstitution.
		if (!isLeftAssigning() && !isRightAssigning()) {
			if (compare(terms.get(0).substitute(partialSubstitution), terms.get(1).substitute(partialSubstitution))) {
				return Collections.singletonList(partialSubstitution);
			} else {
				return Collections.emptyList();
			}
		}
		// Treat case that this is X = t or t = X.
		VariableTerm variable = null;
		Term expression = null;
		if (isLeftAssigning()) {
			variable = (VariableTerm) terms.get(0);
			expression = terms.get(1);
		}
		if (isRightAssigning()) {
			variable = (VariableTerm) terms.get(1);
			expression = terms.get(0);
		}
		Term groundTerm = expression.substitute(partialSubstitution);
		Term resultTerm = null;
		// Check if the groundTerm is an arithmetic expression and evaluate it if so.
		if (groundTerm instanceof ArithmeticTerm) {
			Integer result = ArithmeticTerm.evaluateGroundTerm(groundTerm);
			if (result == null) {
				return Collections.emptyList();
			}
			resultTerm = ConstantTerm.getInstance(result);
		} else {
			// Ground term is another term (constant, or function term).
			resultTerm = groundTerm;
		}
		Substitution extendedSubstitution = new Substitution(partialSubstitution);
		extendedSubstitution.put(variable, resultTerm);
		return Collections.singletonList(extendedSubstitution);
	}

	private boolean compare(Term x, Term y) {
		final int comparison = x.compareTo(y);

		ComparisonOperator operator = isNegated() ? this.operator.getNegation() : this.operator;
		switch (operator) {
			case EQ:
				return comparison ==  0;
			case LT:
				return comparison < 0;
			case GT:
				return comparison > 0;
			case LE:
				return comparison <= 0;
			case GE:
				return comparison >= 0;
			case NE:
				return comparison != 0;
			default:
				throw new UnsupportedOperationException("Unknown comparison operator requested!");
		}
	}

	@Override
	public Type getType() {
		return Type.COMPARISON_ATOM;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(negated ? "not " : "");
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

		if (negated != that.negated) {
			return false;
		}
		if (operator != that.operator) {
			return false;
		}
		return terms.equals(that.terms);
	}

	@Override
	public int hashCode() {
		return 31 * (31 * operator.hashCode() + terms.hashCode()) + (negated ? 1 : 0);
	}
}
