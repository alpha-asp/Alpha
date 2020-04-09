package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * A variable substitution allowing variables to occur on the right-hand side. Chains of variable substitutions are
 * resolved automatically, i.e., adding the substitutions (X -> A) and (A -> d) results in (X -> d), (A -> d).
 * Copyright (c) 2018, the Alpha Team.
 */
public class Unifier extends Substitution {

	private final TreeMap<VariableTerm, List<VariableTerm>> rightHandVariableOccurrences;

	private Unifier(TreeMap<VariableTerm, Term> substitution, TreeMap<VariableTerm, List<VariableTerm>> rightHandVariableOccurrences) {
		if (substitution == null) {
			throw oops("Substitution is null.");
		}
		this.substitution = substitution;
		this.rightHandVariableOccurrences = rightHandVariableOccurrences;
	}

	public Unifier() {
		this(new TreeMap<>(), new TreeMap<>());
	}

	public Unifier(Unifier clone) {
		this(new TreeMap<>(clone.substitution), new TreeMap<>(clone.rightHandVariableOccurrences));
	}

	public Unifier(Substitution clone) {
		this(new TreeMap<>(clone.substitution), new TreeMap<>());
	}


	public Unifier extendWith(Substitution extension) {
		for (Map.Entry<VariableTerm, Term> extensionVariable : extension.substitution.entrySet()) {
			this.put(extensionVariable.getKey(), extensionVariable.getValue());
		}
		return this;
	}

	/**
	 * Unifies the given atoms if possible. Unification will only substitute variables that occur in {@code modifiableAtom}.
	 * @param modifiableAtom an atom that can be modified by substitution
	 * @param unmodifiableAtom an atom that cannot be modified by substitution
	 * @return {@code true} iff unification was successful
	 */
	public boolean unify(Atom modifiableAtom, Atom unmodifiableAtom) {
		if (modifiableAtom == null || unmodifiableAtom == null) {
			throw new IllegalArgumentException("Atom must not be null.");
		}
		if (!Objects.equals(modifiableAtom.getPredicate(), unmodifiableAtom.getPredicate())) {
			throw new IllegalArgumentException("Predicates of two atoms do not match.");
		}
		int i = 0;
		for (Term modifiableTerm : modifiableAtom.getTerms()) {
			if (!unifyTerms(modifiableTerm, unmodifiableAtom.getTerms().get(i), true)) {
				return false;
			}
			i++;
		}
		return true;
	}

	@Override
	public <T extends Comparable<T>> Term put(VariableTerm variableTerm, Term term) {
		// If term is not ground, store it for right-hand side reverse-lookup.
		if (!term.isGround()) {
			for (VariableTerm rightHandVariable : term.getOccurringVariables()) {
				rightHandVariableOccurrences.putIfAbsent(rightHandVariable, new ArrayList<>());
				rightHandVariableOccurrences.get(rightHandVariable).add(variableTerm);
			}
		}
		// Note: We're destroying type information here.
		Term ret = substitution.put(variableTerm, term);

		// Check if the just-assigned variable occurs somewhere in the right-hand side already.
		List<VariableTerm> rightHandOccurrences = rightHandVariableOccurrences.get(variableTerm);
		if (rightHandOccurrences != null) {
			// Replace all occurrences on the right-hand side with the just-assigned term.
			for (VariableTerm rightHandOccurrence : rightHandOccurrences) {
				// Substitute the right hand where this assigned variable occurs with the new value and store it.
				Term previousRightHand = substitution.get(rightHandOccurrence);
				if (previousRightHand == null) {
					// Variable does not occur on the lef-hand side, skip.
					continue;
				}
				substitution.put(rightHandOccurrence, previousRightHand.substitute(this));
			}
		}

		return ret;
	}

	/**
	 * Merge substitution right into left as used in the AnalyzeUnjustified.
	 * Left mappings are seen as equalities, i.e.,
	 * if left has A -> B and right has A -> t then the result will have A -> t and B -> t.
	 * If both substitutions are inconsistent, i.e., A -> t1 in left and A -> t2 in right, then null is returned.
	 * @param left
	 * @param right
	 * @return
	 */
	public static Unifier mergeIntoLeft(Unifier left, Unifier right) {
		// Note: we assume both substitutions are free of chains, i.e., no A->B, B->C but A->C, B->C.
		Unifier ret = new Unifier(left);
		for (Map.Entry<VariableTerm, Term> mapping : right.substitution.entrySet()) {
			VariableTerm variable = mapping.getKey();
			Term term = mapping.getValue();
			// If variable is unset, simply add.
			if (!ret.isVariableSet(variable)) {
				ret.put(variable, term);
				continue;
			}
			// Variable is already set.
			Term setTerm = ret.eval(variable);
			if (setTerm instanceof VariableTerm) {
				// Variable maps to another variable in left.
				// Add a new mapping of the setTerm variable into our right-assigned term.
				ret.put((VariableTerm) setTerm, term);
				// Note: Unifier.put takes care of resolving the chain variable->setTerm->term.
				continue;
			}
			// Check for inconsistency.
			if (setTerm != term) {
				return null;
			}
			// Now setTerm equals term, no action needed.
		}
		return ret;
	}
}
