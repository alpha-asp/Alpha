package at.ac.tuwien.kr.alpha.commons.externals;

import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;

import java.util.List;
import java.util.Set;


// TODO this looks like a duplicate
public interface PredicateInterpretationImpl extends PredicateInterpretation {
	@Override
	Set<List<ConstantTerm<?>>> evaluate(List<Term> terms);
}
