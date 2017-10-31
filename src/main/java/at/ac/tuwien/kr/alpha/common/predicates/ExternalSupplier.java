package at.ac.tuwien.kr.alpha.common.predicates;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ExternalSupplier extends FixedInterpretationPredicate {
	private final Supplier<Set<List<ConstantTerm>>> supplier;

	public ExternalSupplier(String name, Supplier<Set<List<ConstantTerm>>> supplier) {
		super(name, 0);
		this.supplier = supplier;
	}

	@Override
	public Set<List<ConstantTerm>> evaluate(List<Term> terms) {
		if (terms.size() != arity) {
			throw new IllegalArgumentException(name + " can only be used without any arguments.");
		}
		return supplier.get();
	}
}
