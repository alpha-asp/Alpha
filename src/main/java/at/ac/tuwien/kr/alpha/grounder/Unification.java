package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class Unification {

	public static Substitution unifyAtoms(Atom left, Atom right) {
		Substitution mgu = new Substitution();
		if (!left.getPredicate().equals(right.getPredicate())) {
			return null;
		}
		for (int i = 0; i < left.getPredicate().getArity(); i++) {
			final Term leftTerm = left.getTerms().get(i);
			final Term rightTerm = right.getTerms().get(i);
			if (!unifyTerms(leftTerm, rightTerm, mgu)) {
				return null;
			}
		}
		return mgu;
	}

	private static boolean unifyTerms(Term left, Term right, Substitution currentSubstitution) {
		final Term leftSubs = left.substitute(currentSubstitution);
		final Term rightSubs = right.substitute(currentSubstitution);
		// TODO: before adding to substitution, check if variable is not yet set!
		if (leftSubs instanceof VariableTerm) {
			currentSubstitution.put((VariableTerm) leftSubs, rightSubs);
			return true;
		}
		if (rightSubs instanceof VariableTerm) {
			currentSubstitution.put((VariableTerm) rightSubs, leftSubs);
			return true;
		}
		if (leftSubs == rightSubs) {
			return true;
		}
		if (leftSubs instanceof FunctionTerm && rightSubs instanceof FunctionTerm) {
			final FunctionTerm leftFunction = (FunctionTerm) leftSubs;
			final FunctionTerm rightFunction = (FunctionTerm) rightSubs;
			if (!leftFunction.getSymbol().equals(rightFunction.getSymbol())
				|| leftFunction.getTerms().size() != rightFunction.getTerms().size()) {
				return false;
			}
			for (int i = 0; i < leftFunction.getTerms().size(); i++) {
				final Term leftTerm = leftFunction.getTerms().get(i);
				final Term rightTerm = rightFunction.getTerms().get(i);
				if (!unifyTerms(leftTerm, rightTerm, currentSubstitution)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
