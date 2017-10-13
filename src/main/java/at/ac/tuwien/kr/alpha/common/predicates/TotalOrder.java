package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

public class TotalOrder implements Evaluable, Predicate {
	private final String predicateName;

	public TotalOrder(String predicateName) {
		this(predicateName, false);
	}

	public TotalOrder(String predicateName, boolean flip) {
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
	public String getPredicateName() {
		return this.predicateName;
	}

	@Override
	public int getArity() {
		return 2;
	}

	@Override
	public boolean evaluate(List<Term> terms, Substitution substitution) {
		if (terms.size() != getArity()) {
			throw new RuntimeException("Tried to evaluate total order predicate over unexpected arity!");
		}

		final Term x = terms.get(0).substitute(substitution);
		final Term y = terms.get(1).substitute(substitution);

		final int comparison = x.compareTo(y);

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
}
