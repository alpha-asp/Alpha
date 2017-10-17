package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

import java.util.List;

@FunctionalInterface
public interface FixedEvaluable {
	boolean evaluate(List<ConstantTerm> terms);
}
