package at.ac.tuwien.kr.alpha.common.terms;

import at.ac.tuwien.kr.alpha.common.Interner;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This class represents an arithmetic expression occurring as a term.
 * Copyright (c) 2017, the Alpha Team.
 */
public class ArithmeticTerm extends Term {
	private static final Interner<ArithmeticTerm> INTERNER = new Interner<>();
	private final Term left;
	private final ArithmeticOperator arithmeticOperator;
	private final Term right;

	private ArithmeticTerm(Term left, ArithmeticOperator arithmeticOperator, Term right) {
		this.left = left;
		this.arithmeticOperator = arithmeticOperator;
		this.right = right;
	}

	public static ArithmeticTerm getInstance(Term left, ArithmeticOperator arithmeticOperator, Term right) {
		return INTERNER.intern(new ArithmeticTerm(left, arithmeticOperator, right));
	}


	@Override
	public boolean isGround() {
		return left.isGround() && right.isGround();
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		LinkedHashSet<VariableTerm> occurringVariables = new LinkedHashSet<>(left.getOccurringVariables());
		occurringVariables.addAll(right.getOccurringVariables());
		return new ArrayList<>(occurringVariables);
	}

	@Override
	public Term substitute(Substitution substitution) {
		return getInstance(left.substitute(substitution), arithmeticOperator, right.substitute(substitution));
	}

	@Override
	public Term renameVariables(String renamePrefix) {
		return getInstance(left.renameVariables(renamePrefix), arithmeticOperator, right.renameVariables(renamePrefix));
	}

	@Override
	public Term normalizeVariables(String renamePrefix, RenameCounter counter) {
		Term normalizedLeft = left.normalizeVariables(renamePrefix, counter);
		Term normalizedRight = right.normalizeVariables(renamePrefix, counter);
		return ArithmeticTerm.getInstance(normalizedLeft, arithmeticOperator, normalizedRight);

	}

	public static Integer evaluateGroundTerm(Term term) {
		if (!term.isGround()) {
			throw new RuntimeException("Cannot evaluate arithmetic term since it is not ground: " + term);
		}
		return evaluateGroundTermHelper(term);
	}

	private static Integer evaluateGroundTermHelper(Term term) {
		if (term instanceof ConstantTerm
			&& ((ConstantTerm) term).getObject() instanceof Integer) {
			// Extract integer from the constant.
			return (Integer) ((ConstantTerm) term).getObject();
		} else if (term instanceof ArithmeticTerm) {
			return ((ArithmeticTerm) term).evaluateExpression();
		} else {
			// ASP Core 2 standard allows non-integer terms in arithmetic expressions, result is to simply ignore the ground instance.
			return null;
		}
	}

	private Integer evaluateExpression() {
		Integer leftInt = evaluateGroundTermHelper(left);
		Integer rightInt = evaluateGroundTermHelper(right);
		if (leftInt == null || rightInt == null) {
			return  null;
		}
		return arithmeticOperator.eval(leftInt, rightInt);
	}

	@Override
	public String toString() {
		return left + " " + arithmeticOperator + " " + right;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ArithmeticTerm that = (ArithmeticTerm) o;

		if (left != that.left) {
			return false;
		}
		if (arithmeticOperator != that.arithmeticOperator) {
			return false;
		}
		return right == that.right;
	}

	@Override
	public int hashCode() {
		return 31 * (31 * left.hashCode() + arithmeticOperator.hashCode()) + right.hashCode();
	}

	public enum ArithmeticOperator {
		PLUS("+"),
		MINUS("-"),
		TIMES("*"),
		DIV("/");

		private String asString;

		ArithmeticOperator(String asString) {
			this.asString = asString;
		}

		@Override
		public String toString() {
			return asString;
		}

		public Integer eval(Integer left, Integer right) {
			switch (this) {
				case PLUS:
					return left + right;
				case MINUS:
					return left - right;
				case TIMES:
					return left * right;
				case DIV:
					return left / right;
				default:
					throw new RuntimeException("Unknown arithmetic operator encountered.");

			}
		}
	}
}
