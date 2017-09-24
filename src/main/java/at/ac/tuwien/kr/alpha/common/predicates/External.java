package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

public interface External {
	int evaluate(List<Term> input, List<Term> output, Substitution substitution);
}
