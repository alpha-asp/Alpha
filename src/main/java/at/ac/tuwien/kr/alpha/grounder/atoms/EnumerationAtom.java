package at.ac.tuwien.kr.alpha.grounder.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.HashMap;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Represents ground-instance enumeration atom of form: enum(aggId, term, sequenceNo).
 * The semantics of this is: if enum(A,T1, N1) and enum(A,T2,N2) are both true and T1 != T2, then N1 != N2.
 * Furthermore, If enum(A,T1,N1) is true with N>0 then enum(A,T2,N1-1) is true for some T1 != T2
 * and both, T1 and T2, are ground instances the grounder encountered during the search so far.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public class EnumerationAtom extends BasicAtom {
	public static final Predicate ENUMERATION_PREDICATE = Predicate.getInstance("_Enumeration", 3);
	private static final HashMap<Term, HashMap<Term, Integer>> ENUMERATIONS = new HashMap<>();

	public EnumerationAtom(BasicAtom clone) {
		super(ENUMERATION_PREDICATE, clone.getTerms());
		if (getTerms().size() != 3) {
			throw new RuntimeException("EnumerationAtom must have arity three. Given one does not: " + clone);
		}
		/*if (!(getTerms().get(0) instanceof ConstantTerm)) {
			throw new RuntimeException("First parameter of EnumerationAtom must be a constant: " + clone);
		}*/
		if (!(getTerms().get(2) instanceof VariableTerm)) {
			throw new RuntimeException("Third parameter of EnumerationAtom must be a variable: " + clone);
		}
	}

	public static void resetEnumerations() {
		ENUMERATIONS.clear();
	}

	private Integer getEnumerationIndex(Term identifier, Term enumerationTerm) {
		ENUMERATIONS.putIfAbsent(identifier, new HashMap<>());
		HashMap<Term, Integer> enumeratedTerms = ENUMERATIONS.get(identifier);
		Integer assignedInteger = enumeratedTerms.get(enumerationTerm);
		if (assignedInteger == null) {
			int enumerationIndex = enumeratedTerms.size() + 1;
			enumeratedTerms.put(enumerationTerm, enumerationIndex);
			return enumerationIndex;
		} else {
			return assignedInteger;
		}

	}

	public void addEnumerationToSubstitution(Substitution substitution) {
		Term idTerm = getTerms().get(0).substitute(substitution);
		Term enumerationTerm  = getTerms().get(1).substitute(substitution);
		if (!enumerationTerm.isGround()) {
			throw new RuntimeException("Enumeration term is not ground after substitution. Should not happen.");
		}
		Integer enumerationIndex = getEnumerationIndex(idTerm, enumerationTerm);
		substitution.put((VariableTerm) getTerms().get(2), ConstantTerm.getInstance(enumerationIndex));
	}

	@Override
	public EnumerationAtom substitute(Substitution substitution) {
		return new EnumerationAtom(super.substitute(substitution));
	}

	@Override
	public EnumerationLiteral toLiteral(boolean positive) {
		if (!positive) {
			throw oops("IntervalLiteral cannot be negated");
		}
		return new EnumerationLiteral(this);
	}

	@Override
	public EnumerationLiteral toLiteral() {
		return toLiteral(true);
	}
}
