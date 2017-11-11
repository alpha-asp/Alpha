package at.ac.tuwien.kr.alpha.common.interpretations;

import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ExternalSupplier extends FixedInterpretation {
	private final Supplier<Set<List<Constant>>> supplier;

	public ExternalSupplier(Supplier<Set<List<Constant>>> supplier) {
		this.supplier = supplier;
	}

	@Override
	public Set<List<Constant>> evaluate(List<Term> terms) {
		if (!terms.isEmpty()) {
			throw new IllegalArgumentException("Can only be used without any arguments.");
		}
		return supplier.get();
	}
}
