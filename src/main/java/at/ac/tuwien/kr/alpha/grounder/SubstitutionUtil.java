package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.*;

import java.util.ArrayList;

/**
 * This class provides functions for first-order variable substitution.
 * Copyright (c) 2016, the Alpha Team.
 */
public class SubstitutionUtil {

	/**
	 * This method applies a substitution to an atom and returns the AtomId of the ground atom.
	 * @param variableSubstitution the variable substitution to apply.
	 * @return the AtomId of the corresponding substituted ground atom.
	 */
	public static AtomId substitute(AtomStore atomStore, PredicateInstance nonGroundAtom, NaiveGrounder.VariableSubstitution variableSubstitution) {
		Term[] groundTermList = new Term[nonGroundAtom.termList.length];
		for (int i = 0; i < nonGroundAtom.termList.length; i++) {
			Term nonGroundTerm = nonGroundAtom.termList[i];
			Term groundTerm = groundTerm(nonGroundTerm, variableSubstitution);
			groundTermList[i] = groundTerm;
		}
		PredicateInstance groundAtom = new PredicateInstance(nonGroundAtom.predicate, groundTermList);
		return atomStore.createAtomId(groundAtom);
	}

	public static Term groundTerm(Term nonGroundTerm, NaiveGrounder.VariableSubstitution variableSubstitution) {
		if (nonGroundTerm instanceof ConstantTerm) {
			return nonGroundTerm;
		} else if (nonGroundTerm instanceof VariableTerm) {
			Term groundTerm = variableSubstitution.substitution.get(nonGroundTerm);
			if (groundTerm == null) {
				throw new RuntimeException("SubstitutionUtil encountered variable without a substitution given: " + nonGroundTerm);
			}
			return  groundTerm;
		} else if (nonGroundTerm instanceof FunctionTerm) {
			ArrayList<Term> groundTermList = new ArrayList<>(((FunctionTerm) nonGroundTerm).termList.size());
			for (Term term : ((FunctionTerm) nonGroundTerm).termList) {
				groundTermList.add(groundTerm(term, variableSubstitution));
			}
			return FunctionTerm.getFunctionTerm(((FunctionTerm) nonGroundTerm).functionSymbol, groundTermList);
		} else {
			throw new RuntimeException("SubstitutionUtil: Unknown term type encountered.");
		}
	}
}
