package at.ac.tuwien.kr.alpha.common.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Interface for atom whose variables can be normalized, i.e., enumerated from
 * left to right.
 * Copyright (c) 2018 the Alpha Team.
 */
public interface VariableNormalizableAtom {

	Predicate getPredicate();

	List<Term> getTerms();

	/**
	 * Returns an Atom whose variables are enumerated as Vi, .. Vn.
	 * 
	 * @param prefix               the variable prefix V in front of the counter.
	 * @param counterStartingValue the initial value i of the counter.
	 * @return the Atom where all variables are renamed and enumerated
	 *         left-to-right.
	 */
	default Atom normalizeVariables(String prefix, int counterStartingValue) {
		List<Term> renamedTerms = Term.renameTerms(this.getTerms(), prefix, counterStartingValue);
		return new BasicAtom(this.getPredicate(), renamedTerms);
	}
}
