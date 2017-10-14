package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BuiltinAtom implements Literal {
	private final List<Term> terms;
	private final BINOP binop;
	private final boolean isNegated;

	public enum BINOP {
		EQ("=",  new BasicPredicate("=",  2)),
		NE("!=", new BasicPredicate("!=", 2)),
		LT("<",  new BasicPredicate("<",  2)),
		GT(">",  new BasicPredicate(">",  2)),
		LE("<=", new BasicPredicate("<=", 2)),
		GE(">=", new BasicPredicate(">=", 2));

		private String asString;
		private Predicate asPredicate;

		BINOP(String asString, Predicate asPredicate) {
			this.asString = asString;
			this.asPredicate = asPredicate;
		}

		@Override
		public String toString() {
			return asString;
		}

		public Predicate toPredicate() {
			return asPredicate;
		}

		public BINOP getNegation() {
			switch (this) {
				case EQ: return NE;
				case NE: return EQ;
				case LT: return GE;
				case GT: return LE;
				case LE: return GT;
				case GE: return LT;
			}
			throw new RuntimeException("Unknown BINOP encountered, cannot negate it.");
		}
	};



	public BuiltinAtom(BINOP binop, List<Term> terms, boolean isNegated) {
		if (terms.size() != 2) {
			throw new IllegalArgumentException("terms must be of size 2");
		}

		this.binop = binop;
		this.terms = terms;
		this.isNegated = isNegated;
	}

	@Override
	public String toString() {
		return (isNegated ? "not " : "") + terms.get(0) + " " + binop + " " + terms.get(1);
	}

	@Override
	public Predicate getPredicate() {
		return binop.toPredicate();
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
	public boolean isInternal() {
		return false;
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		// For the time being, no variables in built-in atoms can bind.
		List<VariableTerm> vars = new ArrayList<>(2);
		for (Term term : terms) {
			vars.addAll(term.getOccurringVariables());
		}
		return vars;
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		return Collections.emptyList();
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new BuiltinAtom(binop, terms.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()), isNegated);
	}

	public boolean evaluate(Substitution substitution) {
		NumberOrTerm left = evaluateExpression(terms.get(0), substitution);
		NumberOrTerm right = evaluateExpression(terms.get(1), substitution);
		switch (binop) {
			case EQ:
				if (left.isNumber() != right.isNumber()) {
					throw new RuntimeException("BuiltinAtom: cannot compare terms of different types: " + left + binop + right);
				} else if (left.isNumber()) {
					return left.number == right.number;
				} else {
					return left.term.equals(right.term);
				}
			case NE:
				if (left.isNumber() != right.isNumber()) {
					throw new RuntimeException("BuiltinAtom: cannot compare terms of different types: " + left + binop + right);
				} else if (left.isNumber()) {
					return left.number != right.number;
				} else {
					return !left.term.equals(right.term);
				}
			case GT:
				if (left.isNumber() && right.isNumber()) {
					return  left.number > right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
			case LT:
				if (left.isNumber() && right.isNumber()) {
					return  left.number < right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
			case GE:
				if (left.isNumber() && right.isNumber()) {
					return  left.number >= right.number;
				}
				throw new RuntimeException("BuiltinAtom: can only compare number terms: " + left + binop + right);
			case LE:
				if (left.isNumber() && right.isNumber()) {
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

		result = terms.get(0).compareTo(other.terms.get(0));

		if (result != 0) {
			return result;
		}

		return terms.get(1).compareTo(other.terms.get(1));
	}

	@Override
	public boolean isNegated() {
		return isNegated;
	}

	private static class NumberOrTerm {
		public final int number;
		public final Term term;

		public NumberOrTerm(int number) {
			this.number = number;
			this.term = null;
		}

		public NumberOrTerm(Term term) {
			this.term = term;
			this.number = 0;
		}

		public boolean isNumber() {
			return term == null;
		}

		@Override
		public String toString() {
			return term == null ? Integer.toString(number) : term.toString();
		}
	}

	private static NumberOrTerm evaluateExpression(Term term, Substitution substitution) {
		if (term instanceof VariableTerm) {
			return evaluateExpression(substitution.eval((VariableTerm) term), substitution);
		} else if (term instanceof ConstantTerm) {
			try {
				return new NumberOrTerm(Integer.parseInt(term.toString()));
			} catch (NumberFormatException e) {
				return new NumberOrTerm(term);
			}
		} else if (term instanceof FunctionTerm) {
			return new NumberOrTerm(term);
		} else {
			throw new RuntimeException("Not supported term structure in builtin atom encountered: " + term);
		}
	}
}