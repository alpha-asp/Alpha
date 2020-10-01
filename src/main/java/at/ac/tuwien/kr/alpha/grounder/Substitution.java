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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramPartParser;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.Util.oops;

public class Substitution {

	private static final ProgramPartParser PROGRAM_PART_PARSER = new ProgramPartParser();
	public static final Substitution EMPTY_SUBSTITUTION = new Substitution() {
		@Override
		public <T extends Comparable<T>> Term put(VariableTerm variableTerm, Term groundTerm) {
			throw oops("Should not be called on EMPTY_SUBSTITUTION");
		}
	};

	protected TreeMap<VariableTerm, Term> substitution;

	private Substitution(TreeMap<VariableTerm, Term> substitution) {
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

	public static Substitution unify(Literal literal, Instance instance, Substitution substitution) {
		return unify(literal.getAtom(), instance, substitution);
	}

	/**
	 * Helper class to lazily clone the input substitution of Substitution.unify only when needed.
	 */
	private static class UnificationHelper {
		Substitution returnSubstitution;

		Substitution unify(List<Term> termList, Instance instance, Substitution partialSubstitution) {
			for (int i = 0; i < termList.size(); i++) {
				if (termList.get(i) == instance.terms.get(i) ||
					unifyTerms(termList.get(i), instance.terms.get(i), partialSubstitution)) {
					continue;
				}
				return null;
			}
			if (returnSubstitution == null) {
				// All terms unify but there was no need to assign a new variable, return the input substitution.
				return partialSubstitution;
			}
			return returnSubstitution;
		}

		boolean unifyTerms(Term termNonGround, Term termGround, Substitution partialSubstitution) {
			if (termNonGround == termGround) {
				// Both terms are either the same constant or the same variable term
				return true;
			} else if (termNonGround instanceof ConstantTerm) {
				// Since right term is ground, both terms differ
				return false;
			} else if (termNonGround instanceof VariableTerm) {
				VariableTerm variableTerm = (VariableTerm) termNonGround;
				// Left term is variable, bind it to the right term. Use original substitution if it has
				// not been cloned yet.
				Term bound = (returnSubstitution == null ? partialSubstitution : returnSubstitution).eval(variableTerm);
				if (bound != null) {
					// Variable is already bound, return true if binding is the same as the current ground term.
					return termGround == bound;
				}
				// Record new variable binding.
				if (returnSubstitution == null) {
					// Clone substitution if it was not yet.
					returnSubstitution = new Substitution(partialSubstitution);
				}
				returnSubstitution.put(variableTerm, termGround);
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
	 * Computes the unifier of the (nonground) atom, a partial substitution for the atom, and a ground instance.
	 * If unification succeeds the unifying substitution is returned. The partial substitution is left unchanged.
	 *
	 * @param atom         the (potentially nonground) atom to unify.
	 * @param instance     the ground instance to unify with.
	 * @param substitution the (partial) substitution for the atom. This is left unchanged in all cases.
	 * @return null if the unification fails, otherwise it is a unifying substitution. If the parameter substitution
	 * 	already is a unifier, it is returned. If the unifying substitution is an extension of the input
	 * 	substitution, a new substitution will be returned.
	 */
	public static Substitution unify(Atom atom, Instance instance, Substitution substitution) {
		return new UnificationHelper().unify(atom.getTerms(), instance, substitution);
	}


	/**
	 * Checks if a (possibly non-ground) term unifies with a given ground term. Note that this method changes the
	 * {@link Substitution} object such that it becomes the unifier (if possible).
	 * 
	 * @param termNonGround the nonground term.
	 * @param termGround the ground term.
	 * @return true iff both terms unify. If yes, the unifier is available in the {@link Substitution} object this
	 * 	method is called on.
	 */
	public boolean unifyTerms(Term termNonGround, Term termGround) {
		if (termNonGround == termGround) {
			// Both terms are either the same constant or the same variable term
			return true;
		} else if (termNonGround instanceof ConstantTerm) {
			// Since right term is ground, both terms differ
			return false;
		} else if (termNonGround instanceof VariableTerm) {
			VariableTerm variableTerm = (VariableTerm) termNonGround;
			// Left term is variable, bind it to the right term.
			Term bound = eval(variableTerm);

			if (bound != null) {
				// Variable is already bound, return true if binding is the same as the current ground term.
				return termGround == bound;
			}

			substitution.put(variableTerm, termGround);
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
				if (!unifyTerms(ftNonGround.getTerms().get(i), ftGround.getTerms().get(i))) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * This method should be used to obtain the {@link Term} to be used in place of a given {@link VariableTerm} under this substitution.
	 *
	 * @param variableTerm the variable term to substitute, if possible
	 * @return a constant term if the substitution contains the given variable, {@code null} otherwise.
	 */
	public Term eval(VariableTerm variableTerm) {
		return this.substitution.get(variableTerm);
	}

	public <T extends Comparable<T>> Term put(VariableTerm variableTerm, Term groundTerm) {
		if (!groundTerm.isGround()) {
			throw oops("Right-hand term is not ground.");
		}
		Term alreadyAssigned = substitution.get(variableTerm);
		if (alreadyAssigned != null && alreadyAssigned != groundTerm) {
			throw oops("Variable is already assigned to another term.");
		}
		// Note: We're destroying type information here.
		return substitution.put(variableTerm, groundTerm);
	}

	public boolean isEmpty() {
		return substitution.isEmpty();
	}

	public boolean isVariableSet(VariableTerm variable) {
		return substitution.get(variable) != null;
	}

	public Set<VariableTerm> getMappedVariables() {
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
		for (Map.Entry<VariableTerm, Term> e : substitution.entrySet()) {
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
			VariableTerm variable = VariableTerm.getInstance(keyVal[0]);
			Term assignedTerm = PROGRAM_PART_PARSER.parseTerm(keyVal[1]);
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
