package at.ac.tuwien.kr.alpha.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ConstantTerm extends Term {

	public final TermSymbol constantSymbol;

	private static HashMap<ConstantTerm, ConstantTerm> knownConstantTerms = new HashMap<>();

	private ConstantTerm(TermSymbol constantSymbol) {
		this.constantSymbol = constantSymbol;
	}

	public static ConstantTerm getConstantTerm(String constantSymbol) {
		ConstantTerm constantTerm = new ConstantTerm(TermSymbol.getTermSymbol(constantSymbol));
		knownConstantTerms.putIfAbsent(constantTerm, constantTerm);
		return knownConstantTerms.get(constantTerm);
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
