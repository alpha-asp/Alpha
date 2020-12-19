package at.ac.tuwien.kr.alpha.core.grounder.structure;

import static at.ac.tuwien.kr.alpha.core.util.Util.oops;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.VariableNormalizableAtom;
import at.ac.tuwien.kr.alpha.core.grounder.Unification;
import at.ac.tuwien.kr.alpha.core.grounder.Unifier;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class LitSet {
	private final CoreAtom atom;
	private final Set<Unifier> complementSubstitutions;
	private final int hashCode;
	private final CoreAtom normalizedLiteral;
	private final Set<Unifier> normalizedSubstitutions;
	private static int litSetCounter = 1;

	LitSet(CoreAtom atom, Set<Unifier> complementSubstitutions) {
		this.atom = atom.renameVariables("_AS" + litSetCounter++);
		this.complementSubstitutions = new HashSet<>();
		for (Unifier complementSubstitution : complementSubstitutions) {
			if (complementSubstitution == null) {
				throw oops("Unifier is null.");
			}
			Unifier unifyRightAtom = normalizeSubstitution(atom, complementSubstitution, this.atom);
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
	private Unifier normalizeSubstitution(CoreAtom originalAtom, Unifier substitution, CoreAtom normalizedAtom) {
		CoreAtom substitutedLiteral = originalAtom.substitute(substitution);
		return Unification.instantiate(normalizedAtom, substitutedLiteral);
	}

	/**
	 * Checks if the complementSubstitutions exclude everything.
	 * @return true if this LitSet represents the empty set (i.e., everything excluded).
	 */
	public boolean coversNothing() {
		for (Unifier substitution : complementSubstitutions) {
			if (Unification.instantiate(atom.substitute(substitution), atom) != null) {
				return true;
			}
		}
		return false;
	}

	public CoreAtom getAtom() {
		return atom;
	}

	Set<Unifier> getComplementSubstitutions() {
		return Collections.unmodifiableSet(complementSubstitutions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(" + atom + ",{");
		for (Unifier complementSubstitution : complementSubstitutions) {
			sb.append(atom.substitute(complementSubstitution));
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

	private CoreAtom computeNormalized(CoreAtom atom, String prefix) {
		if (atom instanceof VariableNormalizableAtom) {
			return ((VariableNormalizableAtom) atom).normalizeVariables(prefix, 0);
		} else {
			throw oops("Atom to normalize is of unknown type: " + atom);
		}
	}

	private Set<Unifier> computeNormalizedSubstitutions() {
		Set<Unifier> ret = new LinkedHashSet<>();
		for (Unifier substitution : complementSubstitutions) {
			Unifier preNormalizedSubstitution = normalizeSubstitution(atom, substitution, normalizedLiteral);
			// Unifier may still contain variables in the right-hand side, those have to be normalized, too.
			CoreAtom appliedSub = normalizedLiteral.substitute(preNormalizedSubstitution);
			// Apply substitution and normalize all remaining variables, i.e., those appearing at the right-hand side of the substitution.
			CoreAtom normalized = computeNormalized(appliedSub, "_X");
			// Compute final substitution from normalized atom to the one where also variables are normalized.
			Unifier normalizedSubstitution = new Unifier(Unification.instantiate(normalizedLiteral, normalized));
			ret.add(normalizedSubstitution);
		}
		return ret;
	}
}
