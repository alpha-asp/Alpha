package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;

import static java.util.Collections.*;

public interface Evaluable extends Predicate {
	Set<List<ConstantTerm>> TRUE = singleton(emptyList());
	Set<List<ConstantTerm>> FALSE = emptySet();

	Set<List<ConstantTerm>> evaluate(List<Term> terms);
}
