package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.common.VariableTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Substitution {
	private final HashMap<VariableTerm, Term> substitution;

	public Substitution(HashMap<VariableTerm, Term> substitution) {
		this.substitution = substitution;
	}

	public Substitution() {
		this(new HashMap<>());
	}

	public Substitution(Substitution clone) {
		this(new HashMap<>(clone.substitution));
	}

	public void replaceSubstitution(Substitution other) {
		substitution.clear();
		substitution.putAll(other.substitution);
	}

	/**
	 * Prints the variable substitution in a uniform way (sorted by variable names).
	 *
	 * @return
	 */
	public String toUniformString() {
		List<VariableTerm> variablesInSubstitution = new ArrayList<>(substitution.size());
		variablesInSubstitution.addAll(substitution.keySet());
		Collections.sort(variablesInSubstitution); // Hint: Maybe this is a performance issue later, better have sorted/well-defined insertion into VariableSubstitution.
		StringBuilder ret = new StringBuilder();
		for (VariableTerm variableTerm : variablesInSubstitution) {
			ret.append("_")
				.append(variableTerm)
				.append(":")
				.append(substitution.get(variableTerm));
		}
		return ret.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Substitution that = (Substitution) o;

		return substitution != null ? substitution.equals(that.substitution) : that.substitution == null;
	}

	@Override
	public int hashCode() {
		return substitution != null ? substitution.hashCode() : 0;
	}

	public Term eval(VariableTerm variableTerm) {
		return this.substitution.get(variableTerm);
	}

	public Term put(VariableTerm variableTerm, Term groundTerm) {
		return substitution.put(variableTerm, groundTerm);
	}

	public boolean isEmpty() {
		return substitution.isEmpty();
	}
}
