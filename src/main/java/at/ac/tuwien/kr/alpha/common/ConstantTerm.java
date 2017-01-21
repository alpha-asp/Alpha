package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.Substitution;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm extends Term {
	private static final Interner<ConstantTerm> INTERNER = Interners.newStrongInterner();

	private final Symbol symbol;

	private ConstantTerm(String symbol) {
		this.symbol = Symbol.getInstance(symbol);
	}

	public static ConstantTerm getInstance(String constantSymbol) {
		return INTERNER.intern(new ConstantTerm(constantSymbol));
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		return Collections.emptyList();
	}

	@Override
	public Term substitute(Substitution substitution) {
		return this;
	}

	@Override
	public String toString() {
		return symbol.getSymbol();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ConstantTerm that = (ConstantTerm) o;

		return symbol.equals(that.symbol);
	}

	@Override
	public int hashCode() {
		return symbol.hashCode();
	}

	@Override
	public int compareTo(Term o) {
		if (o instanceof ConstantTerm) {
			return symbol.compareTo(((ConstantTerm) o).symbol);
		}
		return 1;
	}
}