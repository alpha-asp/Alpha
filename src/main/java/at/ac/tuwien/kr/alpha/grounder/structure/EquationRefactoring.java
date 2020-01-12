package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.HashSet;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Helpe methods to refactor an equation such that a selected variable occurs alone on the left-hand side of the
 * refactored equation. For example, (S + X) = T becomes X = (T - S) where S and T are arbitrary arithmetic formulas.
 *
 * Copyright (c) 2020, the Alpha Team.
 */
public class EquationRefactoring {

	/**
	 * Rewrites an equation such that the given variable is alone on the left-hand side.
	 * Assumption is that the variable occurs exactly once in the original equation!
	 * @param leftHandSide the left-hand side of the original equation.
	 * @param rightHandSide the right-hand side of the original equation.
	 * @param variable the variable to put alone on the left-hand side.
	 * @return an equation with the variable alone on the left-hand side.
	 */
	private static ComparisonAtom rewriteEquationForVariable(Term leftHandSide, Term rightHandSide, VariableTerm variable) {
		// Stop rewriting if left or right is just the variable.
		if (leftHandSide.equals(variable)) {
			// Check that the variable does not occur on the other side (i.e. it occurred twice in the original equation.
			if (containsVariable(rightHandSide, variable)) {
				throw new CannotRewriteException();
			}
			return new ComparisonAtom(leftHandSide, rightHandSide, ComparisonOperator.EQ);
		}
		if (rightHandSide.equals(variable)) {
			// Check that the variable does not occur on the other side (i.e. it occurred twice in the original equation.
			if (containsVariable(leftHandSide, variable)) {
				throw new CannotRewriteException();
			}
			return new ComparisonAtom(rightHandSide, leftHandSide, ComparisonOperator.EQ);
		}
		// Check whether the variable is in left- or right-hand side.
		Term sideWithVariable;
		Term sideWithoutVariable;
		if (containsVariable(leftHandSide, variable)) {
			sideWithVariable = leftHandSide;
			sideWithoutVariable = rightHandSide;
		} else {
			sideWithVariable = rightHandSide;
			sideWithoutVariable = leftHandSide;
		}
		// Since the side with variable contains more than the variable alone, it must be an arithmetic term.
		if (!(sideWithVariable instanceof ArithmeticTerm)) {
			throw new CannotRewriteException();
		}
		// Move one level of operation from the side with the variable to the side without it.
		// Example: (S + X) = T becomes X = (T - S).
		Term subTermLeft = ((ArithmeticTerm)sideWithVariable).getLeft();
		Term subTermRight = ((ArithmeticTerm)sideWithVariable).getRight();
		Term subTermWithVariable;
		Term subTermWithoutVariable;
		if (containsVariable(subTermLeft, variable)) {
			subTermWithVariable = subTermLeft;
			subTermWithoutVariable = subTermRight;
		} else {
			subTermWithVariable = subTermRight;
			subTermWithoutVariable = subTermLeft;
		}
		// Get the inverse operator to put on the right side.
		ArithmeticTerm.ArithmeticOperator inverseOperator = ((ArithmeticTerm) sideWithVariable).getArithmeticOperator().inverseOperator();
		if (inverseOperator == null) {
			throw new CannotRewriteException();
		}
		Term newRightSide = ArithmeticTerm.getInstance(sideWithoutVariable, inverseOperator, subTermWithoutVariable);
		// We removed just one level, if there are multiple, continue recursively.
		return rewriteEquationForVariable(subTermWithVariable, newRightSide, variable);
	}

	private static boolean containsVariable(Term arithmeticTerm, VariableTerm variable) {
		if (arithmeticTerm instanceof ConstantTerm) {
			return false;
		}
		if (arithmeticTerm instanceof FunctionTerm) {
			return false;
		}
		if (arithmeticTerm instanceof VariableTerm) {
			return arithmeticTerm.equals(variable);
		}
		if (arithmeticTerm instanceof ArithmeticTerm) {
			return containsVariable(((ArithmeticTerm) arithmeticTerm).getLeft(), variable)
				|| containsVariable(((ArithmeticTerm) arithmeticTerm).getRight(), variable);
		}
		if (arithmeticTerm instanceof IntervalTerm) {
			throw oops("Term rewriting for completion cannot handle IntervalTerm" + arithmeticTerm);
		}
		throw oops("Term rewriting for completion cannot handle Term" + arithmeticTerm);
	}

	static ComparisonLiteral transformToUnassignedEqualsRest(Set<VariableTerm> assignedVariables, ComparisonLiteral originalEquation) {
		HashSet<VariableTerm> vars = new HashSet<>(originalEquation.getOccurringVariables());
		vars.removeAll(assignedVariables);
		if (vars.size() != 1) {
			throw new CannotRewriteException();
		}
		VariableTerm unassignedVariable = vars.iterator().next();
		ComparisonAtom comparisonAtom = originalEquation.getAtom();
		ComparisonAtom rewrittenComparisonAtom = rewriteEquationForVariable(comparisonAtom.getTerms().get(0), comparisonAtom.getTerms().get(1), unassignedVariable);
		// Note: if the variable occurs twice, rewriting throws a CannotRewriteException.
		return new ComparisonLiteral(rewrittenComparisonAtom, true);
	}

	static class CannotRewriteException extends RuntimeException {
	}
}
