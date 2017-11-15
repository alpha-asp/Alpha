package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ExternalSupplier extends FixedInterpretation {
	private final Supplier<Set<List<ConstantTerm>>> supplier;

	public ExternalSupplier(Supplier<Set<List<ConstantTerm>>> supplier) {
		this.supplier = supplier;
	}

	@Override
	public Set<List<ConstantTerm>> evaluate(List<Term> terms) {
		if (!terms.isEmpty()) {
			throw new IllegalArgumentException("Can only be used without any arguments.");
		}
		return supplier.get();
	}
}
