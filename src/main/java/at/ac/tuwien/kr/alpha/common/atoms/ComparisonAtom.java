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

	private boolean assignable(Term term) {
		return isNormalizedEquality && term instanceof VariableTerm;
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
		final Term left = terms.get(0);
		final Term right = terms.get(1);
		if (assignable(left) && assignable(right)) {
			// In case this is "X = Y" or "not X != Y" then both sides are binding given that the other is.
			// In this case non-binding and binding variables cannot be reported accurately, in fact, the double variable could be compiled away.
			throw new RuntimeException("Builtin equality with left and right side being variables encountered. Should not happen.");
		}
		if (assignable(left)) {
			return Collections.singletonList((VariableTerm) left);
		}
		if (assignable(right)) {
			return Collections.singletonList((VariableTerm) right);
		}
		return Collections.emptyList();
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		final Term left = terms.get(0);
		final Term right = terms.get(1);
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
			return new ArrayList<>(occurringVariables);
		}
		// Neither left- nor right-assigning, hence no variable is binding.
		return new ArrayList<>(occurringVariables);
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new ComparisonAtom(terms.get(0).substitute(substitution),
			terms.get(1).substitute(substitution),
			negated, operator);
	}

	@Override
	public List<Substitution> getSubstitutions(Substitution partialSubstitution) {
		// Treat case where this is just comparison with all variables bound by partialSubstitution.
		final Term left = terms.get(0).substitute(partialSubstitution);
		final Term right = terms.get(1).substitute(partialSubstitution);
		final boolean leftAssigning = assignable(left);
		final boolean rightAssigning = assignable(right);
		if (!leftAssigning && !rightAssigning) {
			// No assignment (variables are bound by partialSubstitution), thus evaluate comparison only.
			Term leftSubstitute = left;
			Term rightSubstitute = right;
			// Evaluate arithmetics.
			if (left instanceof ArithmeticTerm) {
				Integer leftResult = ArithmeticTerm.evaluateGroundTerm(left);
				if (leftResult == null) {
					return Collections.emptyList();
				}
				leftSubstitute = ConstantTerm.getInstance(leftResult);
			}
			if (right instanceof ArithmeticTerm) {
				Integer rightResult = ArithmeticTerm.evaluateGroundTerm(right);
				if (rightResult == null) {
					return Collections.emptyList();
				}
				rightSubstitute = ConstantTerm.getInstance(rightResult);
			}
			if (compare(leftSubstitute, rightSubstitute)) {
				return Collections.singletonList(partialSubstitution);
			} else {
				return Collections.emptyList();
			}
		}
		// Treat case that this is X = t or t = X.
		VariableTerm variable = null;
		Term expression = null;
		if (leftAssigning) {
			variable = (VariableTerm) terms.get(0);
			expression = terms.get(1);
		}
		if (rightAssigning) {
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
	public String toString() {
		return (negated ? "not " : "") + terms.get(0) + " " + operator + " " + terms.get(1);
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
