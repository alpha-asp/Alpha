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

	public ComparisonLiteral(ComparisonAtom atom, boolean negated) {
		super(atom, negated);
	}
	
	@Override
	public ComparisonAtom getAtom() {
		return (ComparisonAtom)atom;
	}
	
	public boolean isNormalizedEquality() {
		ComparisonOperator operator = getAtom().operator;
		return (!negated && operator == ComparisonOperator.EQ)
				|| (negated && operator == ComparisonOperator.NE);
	}

	private boolean isLeftAssigning() {
		return isNormalizedEquality() && getTerms().get(0) instanceof VariableTerm;
	}

	private boolean isRightAssigning() {
		return isNormalizedEquality() && getTerms().get(1) instanceof VariableTerm;
	}

	/**
	 * Returns a new copy of this literal whose {@link Literal#isNegated()} status is inverted
	 */
	@Override
	public ComparisonLiteral negate() {
		return new ComparisonLiteral(getAtom(), !negated);
	}

	/**
	 * @see Atom#substitute(Substitution)
	 */
	@Override
	public ComparisonLiteral substitute(Substitution substitution) {
		return new ComparisonLiteral(getAtom().substitute(substitution), negated);
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
	public List<Substitution> getSubstitutions(Substitution partialSubstitution) {
		List<Term> terms = getTerms();

		// Treat case where this is just comparison with all variables bound by partialSubstitution.
		if (!isLeftAssigning() && !isRightAssigning()) {
			Term leftSubstitute = terms.get(0).substitute(partialSubstitution);
			Term leftEvaluatedSubstitute = leftSubstitute instanceof ArithmeticTerm ? ConstantTerm.getInstance(evaluateGroundTerm(leftSubstitute)) : leftSubstitute;
			Term rightSubstitute = terms.get(1).substitute(partialSubstitution);
			Term rightEvaluatedSubstitute = rightSubstitute instanceof ArithmeticTerm ? ConstantTerm.getInstance(evaluateGroundTerm(rightSubstitute)) : rightSubstitute;
			if (compare(leftEvaluatedSubstitute, rightEvaluatedSubstitute)) {
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
