package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedBuiltinAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BuiltinAtom implements Atom {
	private final List<Term> terms;
	private final ParsedBuiltinAtom.BINOP binop;

	protected BuiltinAtom(ParsedBuiltinAtom.BINOP binop, List<Term> terms) {
		this.binop = binop;
		this.terms = terms;
	}

	public BuiltinAtom(ParsedBuiltinAtom parsedBuiltinAtom) {
		this(parsedBuiltinAtom.binop, parsedBuiltinAtom.getTerms().stream().map(ParsedTerm::toTerm).collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		return terms.get(0) + " " + binop + " " + terms.get(1);
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
	public List<VariableTerm> getOccurringVariables() {
		List<VariableTerm> vars = new ArrayList<>(2);
		for (Term term : terms) {
			vars.addAll(term.getOccurringVariables());
		}
		return vars;
	}

	@Override
	public Atom substitute(Substitution substitution) {
		return new BuiltinAtom(binop, terms.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()));
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
		} else {
			throw new RuntimeException("Not supported term structure in builtin atom encountered: " + term);
		}
	}
}