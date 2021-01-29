package at.ac.tuwien.kr.alpha.core.atoms;

import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.Util;
import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.VariableTermImpl;
import at.ac.tuwien.kr.alpha.core.grounder.SubstitutionImpl;

/**
 * Represents a ground-instance enumeration atom of form:
 * enum(enumId, groundTerm, sequenceNo).
 * 
 * The semantics of this is:
 * if enum(A,T1, N1) and enum(A,T2,N2) are both true and T1 != T2, then N1 != N2.
 * Furthermore, If enum(A,T1,N1) is true with N1 > 0 then enum(A,T2,N1 - 1) is true for some T1 != T2 and
 * both, T1 and T2, are ground instances the grounder encountered during the search so far.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public class EnumerationAtom extends BasicAtom {
	public static final Predicate ENUMERATION_PREDICATE = CorePredicate.getInstance("_Enumeration", 3);
	private static final HashMap<Term, HashMap<Term, Integer>> ENUMERATIONS = new HashMap<>();

	public EnumerationAtom(List<Term> terms) {
		super(ENUMERATION_PREDICATE, terms);
		if (terms.size() != 3) {
			throw new RuntimeException("EnumerationAtom must have arity three. Given terms are of wrong size: " + terms);
		}
		if (!(getTerms().get(2) instanceof VariableTermImpl)) {
			throw new RuntimeException("Third parameter of EnumerationAtom must be a variable: " + terms);
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

	/**
	 * Based on a given substitution, substitutes the first two terms of this {@link EnumerationAtom} with the values from the substitution,
	 * and returns a new substitution with all mappings from the input substitution plus a binding for the third term of the enum atom to the
	 * integer index that is mapped to the first two terms in the internal <code>ENUMERATIONS</code> map.
	 * 
	 * @param substitution an input substitution which must provide ground terms for the first two terms of the enumeration atom.
	 * @return a new substitution where the third term of the enumeration atom is bound to an integer.
	 */
	public Substitution addEnumerationIndexToSubstitution(Substitution substitution) {
		Term idTerm = this.getTerms().get(0).substitute(substitution);
		Term enumerationTerm = this.getTerms().get(1).substitute(substitution);
		if (!enumerationTerm.isGround()) {
			throw new RuntimeException("Enumeration term is not ground after substitution. Should not happen.");
		}
		Integer enumerationIndex = getEnumerationIndex(idTerm, enumerationTerm);
		SubstitutionImpl retVal = new SubstitutionImpl(substitution);
		retVal.put((VariableTermImpl) getTerms().get(2), CoreConstantTerm.getInstance(enumerationIndex));
		return retVal;
	}

	@Override
	public EnumerationAtom substitute(Substitution substitution) {
		return new EnumerationAtom(super.substitute(substitution).getTerms());
	}

	@Override
	public EnumerationLiteral toLiteral(boolean positive) {
		if (!positive) {
			throw Util.oops("IntervalLiteral cannot be negated");
		}
		return new EnumerationLiteral(this);
	}

	@Override
	public EnumerationLiteral toLiteral() {
		return toLiteral(true);
	}
}
