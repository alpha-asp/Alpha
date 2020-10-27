package at.ac.tuwien.kr.alpha.common.fixedinterpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;

public interface PredicateInterpretationImpl extends PredicateInterpretation {
	@Override
	Set<List<? extends ConstantTerm<?>>> evaluate(List<? extends Term> terms);
}
