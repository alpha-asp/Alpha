package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.VariableNormalizableAtom;
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
	private final Atom atom;
	private final Set<Substitution> complementSubstitutions;
	private final int hashCode;
	private final Atom normalizedLiteral;
	private final Set<Substitution> normalizedSubstitutions;
	private static int litSetCounter = 1;

	LitSet(Atom atom, Set<Substitution> complementSubstitutions) {
		this.atom = atom.renameVariables("_AS" + litSetCounter++);
		this.complementSubstitutions = new HashSet<>();
		for (Substitution complementSubstitution : complementSubstitutions) {
			if (complementSubstitution == null) {
				throw oops("Substitution is null.");
			}
			Substitution unifyRightAtom = normalizeSubstitution(atom, complementSubstitution, this.atom);
			if (unifyRightAtom == null) {
				throw oops("Unification result is null.");
			}
			this.complementSubstitutions.add(unifyRightAtom);
		}
		this.normalizedLiteral = computeNormalized(this.atom, "_N");
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
		return Unification.instantiate(normalizedAtom, substitutedLiteral);
	}

	/**
	 * Checks if the complementSubstitutions exclude everything.
	 * @return true if this LitSet represents the empty set (i.e., everything excluded).
	 */
	public boolean coversNothing() {
		for (Substitution substitution : complementSubstitutions) {
			if (Unification.instantiate(atom.substitute(substitution), atom) != null) {
				return true;
			}
		}
		return false;
	}

	public Atom getAtom() {
		return atom;
	}

	Set<Substitution> getComplementSubstitutions() {
		return Collections.unmodifiableSet(complementSubstitutions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(" + atom + ",{");
		for (Substitution complementSubstitution : complementSubstitutions) {
			sb.append(atom.substitute(complementSubstitution));
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

	private Atom computeNormalized(Atom atom, String prefix) {
		if (atom instanceof VariableNormalizableAtom) {
			return ((VariableNormalizableAtom) atom).normalizeVariables(prefix, 0);
		} else {
			throw oops("Atom to normalize is of unknonw type: " + atom);
		}
	}

	private Set<Substitution> computeNormalizedSubstitutions() {
		Set<Substitution> ret = new LinkedHashSet<>();
		for (Substitution substitution : complementSubstitutions) {
			Substitution preNormalizedSubstitution = normalizeSubstitution(atom, substitution, normalizedLiteral);
			// Substitution may still contain variables in the right-hand side, those have to be normalized, too.
			Atom appliedSub = normalizedLiteral.substitute(preNormalizedSubstitution);
			// Apply substitution and normalize all remaining variables, i.e., those appearing at the right-hand side of the substitution.
			Atom normalized = computeNormalized(appliedSub, "_X");
			// Compute final substitution from normalized atom to the one where also variables are normalized.
			Substitution normalizedSubstitution = Unification.instantiate(normalizedLiteral, normalized);
			ret.add(normalizedSubstitution);
		}
		return ret;
	}
}
