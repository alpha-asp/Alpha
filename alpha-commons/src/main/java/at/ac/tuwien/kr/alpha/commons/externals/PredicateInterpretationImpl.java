package at.ac.tuwien.kr.alpha.commons.externals;

import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

// TODO this looks like a duplicate
public interface PredicateInterpretationImpl extends PredicateInterpretation {
	@Override
	Set<List<ConstantTerm<?>>> evaluate(List<Term> terms);
}
