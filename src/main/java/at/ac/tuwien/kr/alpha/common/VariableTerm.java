package at.ac.tuwien.kr.alpha.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class VariableTerm extends Term implements Comparable<VariableTerm> {
	private final String variableName;

	private static final HashMap<String, VariableTerm> VARIABLES = new HashMap<>();

	private VariableTerm(String variableName) {
		this.variableName = variableName;
	}

	public static VariableTerm getInstance(String variableName) {
		return VARIABLES.computeIfAbsent(variableName, VariableTerm::new);
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		LinkedList<VariableTerm> vars = new LinkedList<>();
		vars.add(this);
		return vars;
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

	@Override
	public int compareTo(VariableTerm o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return 1;
		}
		return variableName.compareTo(o.variableName);
	}
}
