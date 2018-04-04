package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.Unification;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class LitSet {
	private final Atom literal;
	private final Set<Substitution> complementSubstitutions;
	private final int hashCode;
	private final Atom normalizedLiteral;
	private final Set<Substitution> normalizedSubstitutions;
	private static int litSetCounter = 1;

	LitSet(Atom literal, Set<Substitution> complementSubstitutions) {
		this.literal = literal.renameVariables("_AS" + litSetCounter++);
		this.complementSubstitutions = new HashSet<>();
		for (Substitution complementSubstitution : complementSubstitutions) {
			if (complementSubstitution == null) {
				throw oops("Substitution is null.");
			}
			Substitution unifyRightAtom = normalizeSubstitution(literal, complementSubstitution, this.literal);
			if (unifyRightAtom == null) {
				throw oops("Unification result is null.");
			}
			this.complementSubstitutions.add(unifyRightAtom);
		}
		this.normalizedLiteral = computeNormalizedLiteral();
		this.normalizedSubstitutions = computeNormalizedSubstitutions();
		this.hashCode = computeHashCode();
	}

	/**
	 * Normalizes a substitution over the originalAtom into a substitution over the normalizedAtom.
	 * @param originalAtom
	 * @param substitution
	 * @param normalizedAtom
	 * @return
	 */
	private Substitution normalizeSubstitution(Atom originalAtom, Substitution substitution, Atom normalizedAtom) {
		Atom substitutedLiteral = originalAtom.substitute(substitution);
		return Unification.unifyRightAtom(substitutedLiteral, normalizedAtom);
	}

	/**
	 * Checks if the complementSubstitutions exclude everything.
	 * @return true if this LitSet represents the empty set (i.e., everything excluded).
	 */
	public boolean coversNothing() {
		for (Substitution substitution : complementSubstitutions) {
			if (Unification.unifyRightAtom(literal, literal.substitute(substitution)) != null) {
				return true;
			}
		}
		return false;
	}

	public Atom getLiteral() {
		return literal;
	}

	Set<Substitution> getComplementSubstitutions() {
		return Collections.unmodifiableSet(complementSubstitutions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(" + literal + ",{");
		for (Substitution complementSubstitution : complementSubstitutions) {
			sb.append(literal.substitute(complementSubstitution));
			//sb.append(complementSubstitution);
			sb.append(", ");
		}
		sb.append("})");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LitSet litSet = (LitSet) o;

		if (hashCode() != o.hashCode()) {
			return false;
		}

		if (!normalizedLiteral.equals(litSet.normalizedLiteral)) {
			return false;
		}

		return normalizedSubstitutions.equals(litSet.normalizedSubstitutions);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private int computeHashCode() {
		int result = normalizedLiteral.hashCode();
		result = 31 * result + normalizedSubstitutions.hashCode();
		return result;
	}

	private Atom computeNormalizedLiteral() {
		if (literal instanceof BasicAtom) {
			return ((BasicAtom)literal).normalizeVariables("_N", 0);
		} else if (literal instanceof ComparisonAtom) {
			return ((ComparisonAtom) literal).normalizeVariables("_N", 0);
		} else {
			throw oops("Atom to normalize is of unknonw type: " + literal);
		}
	}

	private Set<Substitution> computeNormalizedSubstitutions() {
		Set<Substitution> ret = new LinkedHashSet<>();
		for (Substitution substitution : complementSubstitutions) {
			ret.add(normalizeSubstitution(literal, substitution, normalizedLiteral));
		}
		return ret;
	}
}
