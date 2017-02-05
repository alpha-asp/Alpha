package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.common.VariableTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Substitution {
	private HashMap<VariableTerm, Term> substitution;

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
		this.substitution = other.substitution;
	}

	/**
	 * Checks if the left possible non-ground term unifies with the ground term.
	 * @param termNonGround
	 * @param termGround
	 * @return
	 */
	public boolean unifyTerms(Term termNonGround, Term termGround) {
		if (termNonGround == termGround) {
			// Both terms are either the same constant or the same variable term
			return true;
		} else if (termNonGround instanceof ConstantTerm) {
			// Since right term is ground, both terms differ
			return false;
		} else if (termNonGround instanceof VariableTerm) {
			VariableTerm variableTerm = (VariableTerm)termNonGround;
			// Left term is variable, bind it to the right term.
			if (eval(variableTerm) != null) {
				// Variable is already bound, return true if binding is the same as the current ground term.
				return termNonGround == eval(variableTerm);
			} else {
				substitution.put(variableTerm, termGround);
				return true;
			}
		} else if (termNonGround instanceof FunctionTerm && termGround instanceof FunctionTerm) {
			// Both terms are function terms
			FunctionTerm ftNonGround = (FunctionTerm) termNonGround;
			FunctionTerm ftGround = (FunctionTerm) termGround;

			if (!(ftNonGround.getSymbol().equals(ftGround.getSymbol()))) {
				return false;
			}

			// Iterate over all subterms of both function terms
			for (int i = 0; i < ftNonGround.getTerms().size(); i++) {
				if (!unifyTerms(ftNonGround.getTerms().get(i), ftGround.getTerms().get(i))) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * Prints the variable substitution in a uniform way (sorted by variable names).
	 *
	 * @return
	 */
	public String toUniformString() {
		List<VariableTerm> variablesInSubstitution = new ArrayList<>(substitution.size());
		variablesInSubstitution.addAll(substitution.keySet());
		Collections.sort(variablesInSubstitution); // Hint: Maybe this is a performance issue later, better have sorted/well-defined insertion into Substitution.
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