package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BuiltinAtom implements Atom {
	private final List<Term> terms;
	private final String predicateName;

	public BuiltinAtom(String predicateName, List<Term> terms, boolean flip) {
		if (terms.size() != 2) {
			throw new IllegalArgumentException("terms must be of size 2");
		}

		this.terms = terms;

		if (!flip) {
			this.predicateName = predicateName;
			return;
		}

		switch (predicateName) {
			case "=":
				this.predicateName = "!=";
				return;
			case "<":
				this.predicateName = ">=";
				return;
			case ">":
				this.predicateName = "<=";
				return;
			case "<=":
				this.predicateName = ">";
				return;
			case ">=":
				this.predicateName = "<";
				return;
			case "<>":
			case "!=":
				this.predicateName = "=";
				return;
			default:
				throw new UnsupportedOperationException("Unknown comparison operator (\"" + predicateName + "\") requested!");
		}
	}

	@Override
	public String toString() {
		return terms.get(0) + " " + this.predicateName + " " + terms.get(1);
	}

	@Override
	public Predicate getPredicate() {
		return new BasicPredicate(this.predicateName, 2);
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
		return new BuiltinAtom(this.predicateName, terms.stream().map(t -> {
			return t.substitute(substitution);
		}).collect(Collectors.toList()), false);
	}

	public boolean evaluate(Substitution substitution) {
		final int x = evaluateExpression(terms.get(0), substitution);
		final int y = evaluateExpression(terms.get(1), substitution);

		final int comparison = Integer.compare(x, y);

		switch (this.predicateName) {
			case "=":
				return comparison ==  0;
			case "<":
				return comparison < 0;
			case ">":
				return comparison > 0;
			case "<=":
				return comparison <= 0;
			case ">=":
				return comparison >= 0;
			case "<>":
			case "!=":
				return comparison != 0;
			default:
				throw new UnsupportedOperationException("Unknown comparison operator requested!");
		}
	}

	@Override
	public int compareTo(Atom o) {
		if (!(o instanceof BuiltinAtom)) {
			return 1;
		}

		BuiltinAtom other = (BuiltinAtom)o;

		int result = this.predicateName.compareTo(other.predicateName);

		if (result != 0) {
			return result;
		}

		result = terms.get(0).compareTo(other.terms.get(0));

		if (result != 0) {
			return result;
		}

		return terms.get(1).compareTo(other.terms.get(1));
	}

	private static int evaluateExpression(Term term, Substitution substitution) {
		if (term instanceof VariableTerm) {
			return evaluateExpression(substitution.eval((VariableTerm) term), substitution);
		} else if (term instanceof ConstantTerm) {
			try {
				return Integer.parseInt(term.toString());
			} catch (NumberFormatException e) {
				return ((ConstantTerm) term).getSymbol().getId();
			}
		} else if (term instanceof FunctionTerm) {
			return ((FunctionTerm) term).getSymbol().getId();
		} else {
			throw new UnsupportedOperationException("Unsupported term structure in builtin atom encountered: " + term);
		}
	}
}