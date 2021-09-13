/**
 * Copyright (c) 2017-2019, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;

import static at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm.evaluateGroundTerm;

/**
 * Contains a potentially negated {@link ComparisonAtom}.
 */
public class ComparisonLiteral extends FixedInterpretationLiteral {
	private final boolean isNormalizedEquality;

	public ComparisonLiteral(ComparisonAtom atom, boolean positive) {
		super(atom, positive);
		final ComparisonOperator operator = getAtom().operator;
		isNormalizedEquality = (positive && operator == ComparisonOperator.EQ)
			|| (!positive && operator == ComparisonOperator.NE);
	}
	
	@Override
	public ComparisonAtom getAtom() {
		return (ComparisonAtom)atom;
	}
	
	public boolean isNormalizedEquality() {
		return isNormalizedEquality;
	}

	/**
	 * Returns a new copy of this literal whose {@link Literal#isNegated()} status is inverted
	 */
	@Override
	public ComparisonLiteral negate() {
		return new ComparisonLiteral(getAtom(), !positive);
	}

	/**
	 * @see Atom#substitute(Substitution)
	 */
	@Override
	public ComparisonLiteral substitute(Substitution substitution) {
		return new ComparisonLiteral(getAtom().substitute(substitution), positive);
	}

	private boolean assignable(Term term) {
		return isNormalizedEquality && term instanceof VariableTerm;
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		final Term left = getTerms().get(0);
		final Term right = getTerms().get(1);
		if (assignable(left) && assignable(right)) {
			// In case this is "X = Y" or "not X != Y" then both sides are binding given that the other is.
			// In this case non-binding and binding variables cannot be reported accurately, in fact, the double variable could be compiled away.
			throw new RuntimeException("Builtin equality with left and right side being variables encountered. Should not happen.");
		}
		if (assignable(left)) {
			return Collections.singleton((VariableTerm) left);
		}
		if (assignable(right)) {
			return Collections.singleton((VariableTerm) right);
		}
		return Collections.emptySet();
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		final Term left = getTerms().get(0);
		final Term right = getTerms().get(1);
		HashSet<VariableTerm> occurringVariables = new HashSet<>();
		List<VariableTerm> leftOccurringVariables = new LinkedList<>(left.getOccurringVariables());
		List<VariableTerm> rightOccurringVariables = new LinkedList<>(right.getOccurringVariables());
		if (assignable(left)) {
			leftOccurringVariables.remove(left);
		}
		if (assignable(right)) {
			rightOccurringVariables.remove(right);
		}
		occurringVariables.addAll(leftOccurringVariables);
		occurringVariables.addAll(rightOccurringVariables);
		if (assignable(left) || assignable(right)) {
			return occurringVariables;
		}
		// Neither left- nor right-assigning, hence no variable is binding.
		return occurringVariables;
	}

	@Override
	public List<Substitution> getSatisfyingSubstitutions(Substitution partialSubstitution) {
		// Treat case where this is just comparison with all variables bound by partialSubstitution.
		final Term left = getAtom().getTerms().get(0).substitute(partialSubstitution);
		final Term right = getAtom().getTerms().get(1).substitute(partialSubstitution);
		final boolean leftAssigning = assignable(left);
		final boolean rightAssigning = assignable(right);
		if (!leftAssigning && !rightAssigning) {
			// No assignment (variables are bound by partialSubstitution), thus evaluate comparison only.
			Term leftEvaluatedSubstitute = evaluateTerm(left);
			if (leftEvaluatedSubstitute == null) {
				return Collections.emptyList();
			}
			Term rightEvaluatedSubstitute = evaluateTerm(right);
			if (rightEvaluatedSubstitute == null) {
				return Collections.emptyList();
			}
			if (compare(leftEvaluatedSubstitute, rightEvaluatedSubstitute)) {
				return Collections.singletonList(partialSubstitution);
			} else {
				return Collections.emptyList();
			}
		}
		// Treat case that this is X = t or t = X.
		VariableTerm variable = null;
		Term expression = null;
		if (leftAssigning) {
			variable = (VariableTerm) left;
			expression = right;
		}
		if (rightAssigning) {
			variable = (VariableTerm) right;
			expression = left;
		}
		Term groundTerm = expression.substitute(partialSubstitution);
		Term resultTerm = null;
		// Check if the groundTerm is an arithmetic expression and evaluate it if so.
		if (groundTerm instanceof ArithmeticTerm) {
			Integer result = evaluateGroundTerm(groundTerm);
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
	
	public boolean isLeftOrRightAssigning() {
		final Term left = getTerms().get(0);
		final Term right = getTerms().get(1);
		return isNormalizedEquality && (assignable(left) && right.isGround() || assignable(right) && left.isGround());
	}

	private Term evaluateTerm(Term term) {
		// Evaluate arithmetics.
		if (term instanceof ArithmeticTerm) {
			Integer result = ArithmeticTerm.evaluateGroundTerm(term);
			if (result == null) {
				return null;
			}
			return ConstantTerm.getInstance(result);
		}
		return term;
	}

	private boolean compare(Term x, Term y) {
		final int comparisonResult = x.compareTo(y);
		ComparisonOperator operator = isNegated() ? getAtom().operator.getNegation() : getAtom().operator;
		switch (operator) {
			case EQ:
				return comparisonResult ==  0;
			case LT:
				return comparisonResult < 0;
			case GT:
				return comparisonResult > 0;
			case LE:
				return comparisonResult <= 0;
			case GE:
				return comparisonResult >= 0;
			case NE:
				return comparisonResult != 0;
			default:
				throw new UnsupportedOperationException("Unknown comparison operator requested!");
		}
	}

}
