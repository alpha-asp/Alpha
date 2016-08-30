package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.PredicateInstance;
import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.common.VariableTerm;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

/**
 * This class provides functions for first-order variable substitution.
 * Copyright (c) 2016, the Alpha Team.
 */
public class SubstitutionUtil {

	/**
	 * This method applies a substitution to an atom.
	 * @param variableSubstitution the variable substitution to apply.
	 * @return the atom where all variables are replaced according to the substitution.
	 */
	PredicateInstance substitute(PredicateInstance atom, Map<VariableTerm, Term> variableSubstitution) {
		throw new NotImplementedException("");
	}
}
