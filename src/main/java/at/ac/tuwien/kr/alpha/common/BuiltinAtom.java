package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedBuiltinAtom;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BuiltinAtom implements Atom {
	public final Term left;
	public final Term right;
	public final ParsedBuiltinAtom.BINOP binop;

	public BuiltinAtom(ParsedBuiltinAtom parsedBuiltinAtom) {
		binop = parsedBuiltinAtom.binop;
		left = Term.convertFromParsedTerm(parsedBuiltinAtom.getTerms().get(0));
		right = Term.convertFromParsedTerm(parsedBuiltinAtom.getTerms().get(1));
	}

	@Override
	public String toString() {
		return left + " " + binop + " " + right;
	}

	@Override
	public Predicate getPredicate() {
		return binop.toPredicate();
	}

	@Override
	public boolean isGround() {
		return left.isGround() && right.isGround();
	}

	@Override
	public boolean isInternal() {
		return false;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		List<VariableTerm> vars = new ArrayList<>(2);
		if (left instanceof VariableTerm) {
			vars.add((VariableTerm) left);
		}
		if (right instanceof VariableTerm) {
			vars.add((VariableTerm) right);
		}
		return vars;
	}

	public static boolean evaluateBuiltinAtom(BuiltinAtom builtinAtom, NaiveGrounder.VariableSubstitution variableSubstitution) {
		NumberOrTerm left = evaluateExpression(builtinAtom.left, variableSubstitution);
		NumberOrTerm right = evaluateExpression(builtinAtom.right, variableSubstitution);
		switch (builtinAtom.binop) {
			case EQ:
				if (left.isNumber != right.isNumber) {
					throw new RuntimeException("BuiltinAtom: cannot compare terms of different types: " + left + builtinAtom.binop + right);
				} else if (left.isNumber) {
					return left.number == right.number;
				} else {
					return left.term.equals(right);
				}
			case NE:
				if (left.isNumber != right.isNumber) {
					throw new RuntimeException("BuiltinAtom: cannot compare terms of different types: " + left + builtinAtom.binop + right);
				} else if (left.isNumber) {
					return left.number != right.number;
				} else {
					return !left.term.equals(right);
				}
			case GT:
				if (left.isNumber && right.isNumber) {
					return  left.number > right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + builtinAtom.binop + right);
			case LT:
				if (left.isNumber && right.isNumber) {
					return  left.number < right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + builtinAtom.binop + right);
			case GE:
				if (left.isNumber && right.isNumber) {
					return  left.number >= right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + builtinAtom.binop + right);
			case LE:
				if (left.isNumber && right.isNumber) {
					return  left.number <= right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + builtinAtom.binop + right);
		}
		throw new RuntimeException("Unknown binop: " + builtinAtom.binop);
	}


	private static class NumberOrTerm {
		public final int number;
		public final Term term;
		public final boolean isNumber;

		public NumberOrTerm(int number) {
			this.number = number;
			this.isNumber = true;
			this.term = null;
		}

		public NumberOrTerm(Term term) {
			this.term = term;
			this.isNumber = false;
			this.number = 0;
		}

		@Override
		public String toString() {
			return isNumber ? Integer.toString(number) : term.toString();
		}
	}

	private static NumberOrTerm evaluateExpression(Term term, NaiveGrounder.VariableSubstitution variableSubstitution) {
		if (term instanceof VariableTerm) {
			return evaluateExpression(variableSubstitution.eval((VariableTerm) term), variableSubstitution);
		} else if (term instanceof ConstantTerm) {
			try {
				return new NumberOrTerm(Integer.parseInt(term.toString()));
			} catch (NumberFormatException e) {
				return new NumberOrTerm(term);
			}
		} else {
			throw new RuntimeException("Not supported term structure in builtin atom encountered: " + term);
		}
	}
}
