package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;

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
	public Set<List<ConstantTerm>> evaluate(List<Term> terms) {
		if (terms.size() != getArity()) {
			throw new RuntimeException("Tried to evaluate total order predicate over unexpected arity!");
		}

		final Term x = terms.get(0);
		final Term y = terms.get(1);

		final int comparison = x.compareTo(y);

		boolean result;

		switch (this.predicateName) {
			case "=":
				result = comparison ==  0;
				break;
			case "<":
				result = comparison < 0;
				break;
			case ">":
				result = comparison > 0;
				break;
			case "<=":
				result = comparison <= 0;
				break;
			case ">=":
				result = comparison >= 0;
				break;
			case "<>":
			case "!=":
				result = comparison != 0;
				break;
			default:
				throw new UnsupportedOperationException("Unknown comparison operator requested!");
		}

		return result ? TRUE : FALSE;
	}
}
