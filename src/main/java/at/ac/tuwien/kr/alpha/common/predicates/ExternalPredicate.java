package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.Term;
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
}
