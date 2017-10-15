package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;

public interface External {
	List<Term> evaluate(List<Term> terms);
}
