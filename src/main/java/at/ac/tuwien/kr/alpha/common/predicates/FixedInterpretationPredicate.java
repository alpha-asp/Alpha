package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;

import static java.util.Collections.*;

public abstract class FixedInterpretationPredicate extends Predicate {
	protected static final Set<List<ConstantTerm>> TRUE = singleton(emptyList());
	protected static final Set<List<ConstantTerm>> FALSE = emptySet();

	public FixedInterpretationPredicate(String name, int arity, boolean internal) {
		super(name, arity, internal);
	}

	public static final String EVALUATE_RETURN_TYPE_NAME_PREFIX = Set.class.getName() + "<" + List.class.getName() + "<" + ConstantTerm.class.getName();

	public FixedInterpretationPredicate(String name, int arity) {
		super(name, arity);
	}

	public abstract Set<List<ConstantTerm>> evaluate(List<Term> terms);
}
