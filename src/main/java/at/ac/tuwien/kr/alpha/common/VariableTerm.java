package at.ac.tuwien.kr.alpha.common;

import java.util.HashMap;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class VariableTerm extends Term {
	public final String variableName;

	private static HashMap<VariableTerm, VariableTerm> knownVariableTerms = new HashMap<>();

	private VariableTerm(String variableName) {
		this.variableName = variableName;
	}

	public static VariableTerm getVariableTerm(String variableName) {
		VariableTerm variableTerm = new VariableTerm(variableName);
		knownVariableTerms.putIfAbsent(variableTerm, variableTerm);
		return knownVariableTerms.get(variableTerm);
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public String toString() {
		return variableName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		VariableTerm that = (VariableTerm) o;

		return variableName.equals(that.variableName);

	}

	@Override
	public int hashCode() {
		return variableName.hashCode();
	}
}
