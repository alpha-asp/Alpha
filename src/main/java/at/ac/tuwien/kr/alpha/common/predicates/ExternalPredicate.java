package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

public class ExternalPredicate implements External {
	private final String predicateName;

	private static int add(int a, int b) {
		return a + b;
	}

	public ExternalPredicate(String predicateName) {
		this.predicateName = predicateName;
	}

	@Override
	public int evaluate(List<Term> input, List<Term> output, Substitution substitution) {


		return 0;
	}

	private static int evaluateExpression(Term term, Substitution substitution) {
		if (term instanceof VariableTerm) {
			throw new UnsupportedOperationException("Unsupported term structure in builtin atom encountered: " + term);
		} else if (term instanceof ConstantTerm) {
			try {
				return Integer.parseInt(term.toString());
			} catch (NumberFormatException e) {
				throw new UnsupportedOperationException("Unsupported term structure in builtin atom encountered: " + term);
			}
		} else if (term instanceof FunctionTerm) {
			return ((FunctionTerm) term).getSymbol().getId();
		} else {
			throw new UnsupportedOperationException("Unsupported term structure in builtin atom encountered: " + term);
		}
	}
}
