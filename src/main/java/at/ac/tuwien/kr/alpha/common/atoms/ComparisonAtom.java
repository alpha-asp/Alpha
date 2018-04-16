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
import java.util.stream.Collectors;

/**
 * Represents a builtin atom according to the standard.
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class ComparisonAtom implements FixedInterpretationAtom {
	private final Predicate predicate;
	final ComparisonOperator operator;
	private final List<Term> terms;
	
	ComparisonAtom(List<Term> terms, ComparisonOperator operator) {
		this.terms = terms;
		this.operator = operator;
		this.predicate = Predicate.getInstance(operator.toString(), 2);
	}

	public ComparisonAtom(Term term1, Term term2, ComparisonOperator operator) {
		this(Arrays.asList(term1, term2), operator);
	}

	public boolean isNormalizedEquality() {
		return isNormalizedEquality(false);
	}
	
	public boolean isNormalizedEquality(boolean negated) {
		return (!negated && operator == ComparisonOperator.EQ)
				|| (negated && operator == ComparisonOperator.NE);
	}
	
	boolean isLeftAssigning() {
		return isLeftAssigning(false);
	}

	private boolean isLeftAssigning(boolean negated) {
		return isNormalizedEquality(negated) && terms.get(0) instanceof VariableTerm;
	}
	
	boolean isRightAssigning() {
		return isRightAssigning(false);
	}

	private boolean isRightAssigning(boolean negated) {
		return isNormalizedEquality(negated) && terms.get(1) instanceof VariableTerm;
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
	public Set<VariableTerm> getBindingVariables(boolean negated) {
		if (isLeftAssigning(negated) && isRightAssigning(negated)) {
			// In case this is "X = Y" or "not X != Y" then both sides are binding given that the other is.
			// In this case non-binding and binding variables cannot be reported accurately, in fact, the double variable could be compiled away.
			throw new RuntimeException("Builtin equality with left and right side being variables encountered. Should not happen.");
		}
		if (isLeftAssigning(negated)) {
			return Collections.singleton((VariableTerm) terms.get(0));
		}
		if (isRightAssigning(negated)) {
			return Collections.singleton((VariableTerm) terms.get(1));
		}
		return Collections.emptySet();
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables(boolean negated) {
		Term left = terms.get(0);
		Term right = terms.get(1);
		HashSet<VariableTerm> occurringVariables = new HashSet<>();
		List<VariableTerm> leftOccurringVariables = new LinkedList<>(left.getOccurringVariables());
		List<VariableTerm> rightOccurringVariables = new LinkedList<>(right.getOccurringVariables());
		if (isLeftAssigning(negated)) {
			leftOccurringVariables.remove(left);
		}
		if (isRightAssigning(negated)) {
			rightOccurringVariables.remove(right);
		}
		occurringVariables.addAll(leftOccurringVariables);
		occurringVariables.addAll(rightOccurringVariables);
		if (isLeftAssigning(negated) || isRightAssigning(negated)) {
			return occurringVariables;
		}
		// Neither left- nor right-assigning, hence no variable is binding.
		return occurringVariables;
	}

	@Override
	public ComparisonAtom substitute(Substitution substitution) {
		List<Term> substitutedTerms = terms.stream().map(t -> t.substitute(substitution)).collect(Collectors.toList());
		return new ComparisonAtom(substitutedTerms, operator);
	}

	@Override
	public List<Substitution> getSubstitutions(Substitution partialSubstitution, boolean negated) {
		return getSubstitutions(this, partialSubstitution, negated);
	}

	static List<Substitution> getSubstitutions(ComparisonAtom atom, Substitution partialSubstitution, boolean negated) {
		ComparisonOperator operator = negated ? atom.operator.getNegation() : atom.operator;
		
		// Treat case where this is just comparison with all variables bound by partialSubstitution.
		if (!atom.isLeftAssigning(negated) && !atom.isRightAssigning(negated)) {
			if (compare(atom.terms.get(0).substitute(partialSubstitution), atom.terms.get(1).substitute(partialSubstitution), operator)) {
				return Collections.singletonList(partialSubstitution);
			} else {
				return Collections.emptyList();
			}
		}
		// Treat case that this is X = t or t = X.
		VariableTerm variable = null;
		Term expression = null;
		if (atom.isLeftAssigning(negated)) {
			variable = (VariableTerm) atom.terms.get(0);
			expression = atom.terms.get(1);
		}
		if (atom.isRightAssigning(negated)) {
			variable = (VariableTerm) atom.terms.get(1);
			expression = atom.terms.get(0);
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

	static boolean compare(Term x, Term y, final ComparisonOperator operator) {
		int comparisonResult = x.compareTo(y);
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
}
