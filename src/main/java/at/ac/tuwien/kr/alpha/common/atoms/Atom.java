package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public interface Atom extends Comparable<Atom> {
	Predicate getPredicate();
	List<Term> getTerms();

	boolean isGround();

	/**
	 * List of all variables occurring in the Atom that are potentially binding, i.e., variables in positive atoms.
	 * @return
	 */
	List<VariableTerm> getBindingVariables();

	/**
	 * List of all variables occurring in the Atom that are never binding, not even in positive atoms, e.g., variables in intervals or built-in atoms.
	 * @return
	 */
	List<VariableTerm> getNonBindingVariables();

	/**
	 * This method applies a substitution to a potentially non-substitute atom.
	 * The resulting atom may be non-substitute.
	 * @param substitution the variable substitution to apply.
	 * @return the atom resulting from the applying the substitution.
	 */
	Atom substitute(Substitution substitution);

	default Atom renameVariables(String newVariablePrefix) {
		ArrayList<VariableTerm> occurringVariables = new ArrayList<>(getBindingVariables());
		occurringVariables.addAll(getNonBindingVariables());
		Substitution renamingSubstitution = new Substitution();
		int counter = 0;
		for (VariableTerm variable : occurringVariables) {
			renamingSubstitution.put(variable, VariableTerm.getInstance(newVariablePrefix + counter++));
		}
		return this.substitute(renamingSubstitution);
	}

	@Override
	default int compareTo(Atom o) {
		if (o == null) {
			return 1;
		}

		final List<Term> aTerms = this.getTerms();
		final List<Term> bTerms = o.getTerms();

		if (aTerms.size() != bTerms.size()) {
			return Integer.compare(aTerms.size(), bTerms.size());
		}

		int result = this.getPredicate().compareTo(o.getPredicate());

		if (result != 0) {
			return result;
		}

		for (int i = 0; i < aTerms.size(); i++) {
			result = aTerms.get(i).compareTo(o.getTerms().get(i));
			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}
