package at.ac.tuwien.kr.alpha.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm extends Term {
	private final TermSymbol constantSymbol;

	private static final HashMap<String, ConstantTerm> CONSTANTS = new HashMap<>();

	private ConstantTerm(String constantSymbol) {
		this.constantSymbol = TermSymbol.getInstance(constantSymbol);
	}

	public static ConstantTerm getInstance(String constantSymbol) {
		return CONSTANTS.computeIfAbsent(constantSymbol, ConstantTerm::new);
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		return new LinkedList<>();
	}

	@Override
	public String toString() {
		return constantSymbol.getSymbol();
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

		return constantSymbol.equals(that.constantSymbol);

	}

	@Override
	public int hashCode() {
		return constantSymbol.hashCode();
	}
}
