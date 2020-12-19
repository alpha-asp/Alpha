package at.ac.tuwien.kr.alpha.core.common.fixedinterpretations;

import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

public interface PredicateInterpretationImpl extends PredicateInterpretation {
	@Override
	Set<List<? extends ConstantTerm<?>>> evaluate(List<? extends Term> terms);
}
