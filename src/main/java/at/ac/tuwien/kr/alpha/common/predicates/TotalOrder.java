package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
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
