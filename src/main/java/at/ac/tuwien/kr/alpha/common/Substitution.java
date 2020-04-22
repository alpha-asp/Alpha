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
package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramPartParser;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.Util.oops;

public class Substitution {

	private static final ProgramPartParser PROGRAM_PART_PARSER = new ProgramPartParser();

	protected TreeMap<VariableTerm, Term> substitution;

	Substitution(TreeMap<VariableTerm, Term> substitution) {
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
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param substitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	public static Substitution unify(Atom atom, Instance instance, Substitution substitution) {
		for (int i = 0; i < instance.terms.size(); i++) {
			if (instance.terms.get(i) == atom.getTerms().get(i) ||
				substitution.unifyTerms(atom.getTerms().get(i), instance.terms.get(i))) {
				continue;
			}
			return null;
		}
		return substitution;
	}

	/**
	 * Checks if the left, possibly non-ground term unifies with the right ground term.
	 * This substitution is modified to reflect the unification.
	 * @param termNonGround the left term, possibly non-ground
	 * @param termGround the right term, must be ground
	 * @return {@code true} iff the unification succeeds
	 */
	public boolean unifyTerms(Term termNonGround, Term termGround) {
		return unifyTerms(termNonGround, termGround, false);
	}

	/**
	 * Checks if the left, possibly non-ground term unifies with the right, possibly non-ground term.
	 * This substitution is modified to reflect the unification.
	 * @param termLeft the left term, possibly non-ground
	 * @param termRight the right term, may only be non-ground if {@code allowNonGroundTermRight} is {@code true}
	 * @return {@code true} iff the unification succeeds
	 */
	public boolean unifyTerms(Term termLeft, Term termRight, boolean allowNonGroundTermRight) {
		final boolean isRightTermGround = termRight.isGround();
		if (!allowNonGroundTermRight && !isRightTermGround) {
			throw new IllegalArgumentException("Term " + termRight + " is not ground.");
		}
		if (termLeft == termRight) {
			return true;
		} else if (termLeft instanceof ConstantTerm) {
			return false;
		} else if (termLeft instanceof VariableTerm) {
			VariableTerm variableTermLeft = (VariableTerm)termLeft;
			if (isRightTermGround) {
				// Left term is variable, bind it to the right term.
				Term bound = eval(variableTermLeft);

				if (bound != null) {
					// Variable is already bound, return true if binding is the same as the current ground term.
					return termRight == bound;
				}
			}
			put(variableTermLeft, termRight);
			return true;
		} else if (termLeft instanceof FunctionTerm && termRight instanceof FunctionTerm) {
			// Both terms are function terms
			FunctionTerm ftNonGround = (FunctionTerm) termLeft;
			FunctionTerm ftGround = (FunctionTerm) termRight;

			if (!(ftNonGround.getSymbol().equals(ftGround.getSymbol()))) {
				return false;
			}
			if (ftNonGround.getTerms().size() != ftGround.getTerms().size()) {
				return false;
			}

			// Iterate over all subterms of both function terms
			for (int i = 0; i < ftNonGround.getTerms().size(); i++) {
				if (!unifyTerms(ftNonGround.getTerms().get(i), ftGround.getTerms().get(i), allowNonGroundTermRight)) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * This method should be used to obtain the {@link Term} to be used in place of
	 * a given {@link VariableTerm} under this substitution.
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
		String assignments[] = bare.split(",");
		Substitution ret = new Substitution();
		for (String assignment : assignments) {
			String keyVal[] = assignment.split("->");
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
