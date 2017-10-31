package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Represents ground-instance enumeration atom of form: enum(aggId, term, sequenceNo).
 * The semantics of this is: if enum(A,T1, N1) and enum(A,T2,N2) are both true and T1 != T2, then N1 != N2.
 * Furthermore, If enum(A,T1,N1) is true with N>0 then enum(A,T2,N1-1) is true for some T1 != T2
 * and both, T1 and T2, are ground instances the grounder encountered during the search so far.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public class EnumerationAtom extends BasicAtom {
	public static final Predicate ENUMERATION_PREDICATE = new Predicate("_Enumeration", 3);
	private static final HashMap<ConstantTerm, HashMap<Term, Integer>> ENUMERATIONS = new HashMap<>();

	public EnumerationAtom(BasicAtom clone) {
		super(ENUMERATION_PREDICATE, clone.getTerms(), clone.isNegated());
		if (getTerms().size() != 3) {
			throw new RuntimeException("EnumerationAtom must have arity three. Given one does not: " + clone);
		}
		if (!(getTerms().get(0) instanceof ConstantTerm)) {
			throw new RuntimeException("First parameter of EnumerationAtom must be a constant: " + clone);
		}
		if (!(getTerms().get(2) instanceof VariableTerm)) {
			throw new RuntimeException("Third parameter of EnumerationAtom must be a variable: " + clone);
		}
	}

	public static void resetEnumerations() {
		ENUMERATIONS.clear();
	}

	private Integer getEnumerationIndex(ConstantTerm identifier, Term enumerationTerm) {
		ENUMERATIONS.putIfAbsent(identifier, new HashMap<>());
		HashMap<Term, Integer> enumeratedTerms = ENUMERATIONS.get(identifier);
		Integer assignedInteger = enumeratedTerms.get(enumerationTerm);
		if (assignedInteger == null) {
			int enumerationIndex = enumeratedTerms.size();
			enumeratedTerms.put(enumerationTerm, enumerationIndex);
			return enumerationIndex;
		} else {
			return assignedInteger;
		}

	}

	public void addEnumerationToSubstitution(Substitution substitution) {
		ConstantTerm idTerm = (ConstantTerm) getTerms().get(0);
		Term enumerationTerm  = getTerms().get(1).substitute(substitution);
		if (!enumerationTerm.isGround()) {
			throw new RuntimeException("Enumeration term is not ground after substitution. Should not happen.");
		}
		Integer enumerationIndex = getEnumerationIndex(idTerm, enumerationTerm);
		substitution.put((VariableTerm) getTerms().get(2), ConstantTerm.getInstance(enumerationIndex));
	}

	@Override
	public List<VariableTerm> getBindingVariables() {
		return Collections.singletonList((VariableTerm) getTerms().get(2));
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		List<VariableTerm> ret = new ArrayList<>(2);
		Term idTerm = getTerms().get(0);
		Term enumTerm = getTerms().get(1);
		if (idTerm instanceof VariableTerm) {
			ret.add((VariableTerm) idTerm);
		}
		if (enumTerm instanceof VariableTerm) {
			ret.add((VariableTerm) enumTerm);
		}
		return ret;
	}
}
