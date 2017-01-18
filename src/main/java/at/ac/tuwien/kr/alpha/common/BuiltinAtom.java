package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedBuiltinAtom;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BuiltinAtom implements Atom {
	private final Term left;
	private final Term right;
	private final ParsedBuiltinAtom.BINOP binop;

	public BuiltinAtom(ParsedBuiltinAtom parsedBuiltinAtom) {
		binop = parsedBuiltinAtom.binop;
		left = parsedBuiltinAtom.terms.get(0).toTerm();
		right = parsedBuiltinAtom.terms.get(1).toTerm();
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
	public Term[] getTerms() {
		return new Term[]{left, right};
	}

	@Override
	public boolean isGround() {
		return left.isGround() && right.isGround();
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

	public boolean evaluate(NaiveGrounder.VariableSubstitution variableSubstitution) {
		NumberOrTerm left = evaluateExpression(this.left, variableSubstitution);
		NumberOrTerm right = evaluateExpression(this.right, variableSubstitution);
		switch (binop) {
			case EQ:
				if (left.isNumber != right.isNumber) {
					throw new RuntimeException("BuiltinAtom: cannot compare terms of different types: " + left + binop + right);
				} else if (left.isNumber) {
					return left.number == right.number;
				} else {
					return left.term.equals(right.term);
				}
			case NE:
				if (left.isNumber != right.isNumber) {
					throw new RuntimeException("BuiltinAtom: cannot compare terms of different types: " + left + binop + right);
				} else if (left.isNumber) {
					return left.number != right.number;
				} else {
					return !left.term.equals(right.term);
				}
			case GT:
				if (left.isNumber && right.isNumber) {
					return  left.number > right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
			case LT:
				if (left.isNumber && right.isNumber) {
					return  left.number < right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
			case GE:
				if (left.isNumber && right.isNumber) {
					return  left.number >= right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
			case LE:
				if (left.isNumber && right.isNumber) {
					return  left.number <= right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
		}
		throw new RuntimeException("Unknown binop: " + binop);
	}

	@Override
	public int compareTo(Atom o) {
		if (!(o instanceof BuiltinAtom)) {
			return 1;
		}

		BuiltinAtom other = (BuiltinAtom)o;

		int result = binop.compareTo(other.binop);

		if (result != 0) {
			return result;
		}

		result = left.compareTo(other.left);

		if (result != 0) {
			return result;
		}

		return right.compareTo(other.right);
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