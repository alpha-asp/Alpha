package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;

@FunctionalInterface
public interface Evaluable {
	boolean evaluate(List<Term> terms, Substitution substitution);
}
