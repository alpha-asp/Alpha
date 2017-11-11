package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;

import static java.util.Collections.*;

public abstract class FixedInterpretation {
	protected static final Set<List<Constant>> TRUE = singleton(emptyList());
	protected static final Set<List<Constant>> FALSE = emptySet();

	public static final String EVALUATE_RETURN_TYPE_NAME_PREFIX = Set.class.getName() + "<" + List.class.getName() + "<" + Constant.class.getName();

	public abstract Set<List<Constant>> evaluate(List<Term> terms);
}
