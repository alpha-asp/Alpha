/*
 * Copyright (c) 2018, 2020-2022, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * A variable substitution allowing variables to occur on the right-hand side. Chains of variable substitutions are
 * resolved automatically, i.e., adding the substitutions {@literal (X -> A)} and {@literal (A -> d)} results in {@literal (X -> d)}, {@literal (A -> d)}.
 * Copyright (c) 2018-2020, the Alpha Team.
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
	 * Returns a list of all variables occurring in that unifier, i.e., variables that are mapped and those that occur (nested) in the right-hand side of the unifier.
	 * @return the list of variables occurring somewhere in the unifier.
	 */
	@Override
	public Set<VariableTerm> getMappedVariables() {
		Set<VariableTerm> ret = new HashSet<>();
		for (Map.Entry<VariableTerm, Term> substitution : substitution.entrySet()) {
			ret.add(substitution.getKey());
			ret.addAll(substitution.getValue().getOccurringVariables());
		}
		return ret;
	}


	@Override
	public <T extends Comparable<T>> Term put(VariableTerm variableTerm, Term term) {
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

		// If term is not ground, store it for right-hand side reverse-lookup.
		if (!term.isGround()) {
			for (VariableTerm rightHandVariable : term.getOccurringVariables()) {
				rightHandVariableOccurrences.putIfAbsent(rightHandVariable, new ArrayList<>());
				rightHandVariableOccurrences.get(rightHandVariable).add(variableTerm);
			}
		}

		return ret;
	}

	/**
	 * Merge substitution right into left as used in the AnalyzeUnjustified.
	 * Left mappings are seen as equalities, i.e.,
	 * if left has {@literal A -> B} and right has {@literal A -> t} then the result will have {@literal A -> t} and {@literal B -> t}.
	 * If both substitutions are inconsistent, i.e., {@literal A -> t1} in left and {@literal A -> t2} in right, then null is returned.
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
