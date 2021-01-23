/*
 * Copyright (c) 2016-2020, the Alpha Team.
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
package at.ac.tuwien.kr.alpha.core.grounder;

import static at.ac.tuwien.kr.alpha.core.util.Util.oops;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.VariableTermImpl;
import at.ac.tuwien.kr.alpha.core.parser.ProgramPartParser;

public class Substitution {

	private static final ProgramPartParser PROGRAM_PART_PARSER = new ProgramPartParser();
	public static final Substitution EMPTY_SUBSTITUTION = new Substitution() {
		@Override
		public <T extends Comparable<T>> CoreTerm put(VariableTermImpl variableTerm, CoreTerm groundTerm) {
			throw oops("Should not be called on EMPTY_SUBSTITUTION");
		}
	};

	protected TreeMap<VariableTermImpl, CoreTerm> substitution;

	private Substitution(TreeMap<VariableTermImpl, CoreTerm> substitution) {
		if (substitution == null) {
			throw oops("Substitution is null.");
		}
		this.substitution = substitution;
	}

	public Substitution() {
		this(new TreeMap<>());
	}

	public Substitution(Substitution clone) {
		this(new TreeMap<>(clone.substitution));
	}

	public static Substitution specializeSubstitution(CoreLiteral literal, Instance instance, Substitution substitution) {
		return specializeSubstitution(literal.getAtom(), instance, substitution);
	}

	/**
	 * Helper class to lazily clone the input substitution of Substitution.specializeSubstitution only when needed.
	 */
	private static class SpecializationHelper {
		Substitution updatedSubstitution;	// Is null for as long as the given partial substitution is not extended, afterwards holds the updated/extended/specialized substitution.

		Substitution unify(List<? extends CoreTerm> termList, Instance instance, Substitution partialSubstitution) {
			for (int i = 0; i < termList.size(); i++) {
				if (!unifyTerms(termList.get(i), instance.terms.get(i), partialSubstitution)) {
					return null;
				}
			}
			if (updatedSubstitution == null) {
				// All terms unify but there was no need to assign a new variable, return the input substitution.
				return partialSubstitution;
			}
			return updatedSubstitution;
		}

		boolean unifyTerms(CoreTerm termNonGround, CoreTerm termGround, Substitution partialSubstitution) {
			if (termNonGround == termGround) {
				// Both terms are either the same constant or the same variable term
				return true;
			} else if (termNonGround instanceof CoreConstantTerm) {
				// Since right term is ground, both terms differ
				return false;
			} else if (termNonGround instanceof VariableTermImpl) {
				VariableTermImpl variableTerm = (VariableTermImpl) termNonGround;
				// Left term is variable, bind it to the right term. Use original substitution if it has
				// not been cloned yet.
				CoreTerm bound = (updatedSubstitution == null ? partialSubstitution : updatedSubstitution).eval(variableTerm); // Get variable binding, either from input substitution if it has not been updated yet, or from the cloned/updated substitution.
				if (bound != null) {
					// Variable is already bound, return true if binding is the same as the current ground term.
					return termGround == bound;
				}
				// Record new variable binding.
				if (updatedSubstitution == null) {
					// Clone substitution if it was not yet updated.
					updatedSubstitution = new Substitution(partialSubstitution);
				}
				updatedSubstitution.put(variableTerm, termGround);
				return true;
			} else if (termNonGround instanceof FunctionTerm && termGround instanceof FunctionTerm) {
				// Both terms are function terms
				FunctionTerm ftNonGround = (FunctionTerm) termNonGround;
				FunctionTerm ftGround = (FunctionTerm) termGround;

				if (!(ftNonGround.getSymbol().equals(ftGround.getSymbol()))) {
					return false;
				}
				if (ftNonGround.getTerms().size() != ftGround.getTerms().size()) {
					return false;
				}

				// Iterate over all subterms of both function terms
				for (int i = 0; i < ftNonGround.getTerms().size(); i++) {
					if (!unifyTerms(ftNonGround.getTerms().get(i), ftGround.getTerms().get(i), partialSubstitution)) {
						return false;
					}
				}

				return true;
			}
			return false;
		}
	}

	/**
	 * Specializes a given substitution such that applying the specialized substitution on the given atom yields the
	 * given instance (if such a specialized substitution exists). Computes the unifier of the (nonground) atom and
	 * the given ground instance such that the unifier is an extension of the given partial substitution. If
	 * specialization succeeds the unifying substitution is returned, if no such unifier exists null is returned. In
	 * any case the partial substitution is left unchanged.
	 *
	 * @param atom         the (potentially nonground) atom to unify.
	 * @param instance     the ground instance to unify the atom with.
	 * @param substitution the (partial) substitution for the atom. This is left unchanged in all cases.
	 * @return null if the unification/specialization fails, otherwise it is a unifying substitution. If the
	 * 	parameter substitution already is a unifier, it is returned. If the unifying substitution is an
	 * 	extension of the input substitution, a new substitution will be returned.
	 */
	public static Substitution specializeSubstitution(CoreAtom atom, Instance instance, Substitution substitution) {
		return new SpecializationHelper().unify(atom.getTerms(), instance, substitution);
	}

	/**
	 * This method should be used to obtain the {@link CoreTerm} to be used in place of a given {@link VariableTermImpl} under this substitution.
	 *
	 * @param variableTerm the variable term to substitute, if possible
	 * @return a constant term if the substitution contains the given variable, {@code null} otherwise.
	 */
	public CoreTerm eval(VariableTermImpl variableTerm) {
		return this.substitution.get(variableTerm);
	}

	public <T extends Comparable<T>> CoreTerm put(VariableTermImpl variableTerm, CoreTerm groundTerm) {
		if (!groundTerm.isGround()) {
			throw oops("Right-hand term is not ground.");
		}
		CoreTerm alreadyAssigned = substitution.get(variableTerm);
		if (alreadyAssigned != null && alreadyAssigned != groundTerm) {
			throw oops("Variable is already assigned to another term.");
		}
		// Note: We're destroying type information here.
		return substitution.put(variableTerm, groundTerm);
	}

	public boolean isEmpty() {
		return substitution.isEmpty();
	}

	public boolean isVariableSet(VariableTermImpl variable) {
		return substitution.get(variable) != null;
	}

	public Set<VariableTermImpl> getMappedVariables() {
		return substitution.keySet();
	}

	/**
	 * Prints the variable substitution in a uniform way (sorted by variable names).
	 *
	 * @return
	 */
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder("{");
		boolean isFirst = true;
		for (Map.Entry<VariableTermImpl, CoreTerm> e : substitution.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				ret.append(",");
			}
			ret.append(e.getKey()).append("->").append(e.getValue());
		}
		ret.append("}");
		return ret.toString();
	}

	public static Substitution fromString(String substitution) {
		String bare = substitution.substring(1, substitution.length() - 1);
		String[] assignments = bare.split(",");
		Substitution ret = new Substitution();
		for (String assignment : assignments) {
			String[] keyVal = assignment.split("->");
			VariableTermImpl variable = VariableTermImpl.getInstance(keyVal[0]);
			CoreTerm assignedTerm = PROGRAM_PART_PARSER.parseTerm(keyVal[1]);
			ret.put(variable, assignedTerm);
		}
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Substitution that = (Substitution) o;

		return Objects.equals(substitution, that.substitution);
	}

	@Override
	public int hashCode() {
		return substitution != null ? substitution.hashCode() : 0;
	}
}
