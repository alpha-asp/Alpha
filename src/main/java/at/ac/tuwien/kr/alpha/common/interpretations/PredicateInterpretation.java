package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;

import static java.util.Collections.*;

@FunctionalInterface
public interface PredicateInterpretation {
	Set<List<ConstantTerm>> TRUE = singleton(emptyList());
	Set<List<ConstantTerm>> FALSE = emptySet();

	String EVALUATE_RETURN_TYPE_NAME_PREFIX = Set.class.getName() + "<" + List.class.getName() + "<" + ConstantTerm.class.getName();

	Set<List<ConstantTerm>> evaluate(List<Term> terms);
}
