/**
 * Copyright (c) 2016-2018, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.Substitutable;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Substitution {
	private TreeMap<VariableTerm, Term> substitution;

	private Substitution(TreeMap<VariableTerm, Term> substitution) {
		this.substitution = substitution;
	}

	public Substitution() {
		this(new TreeMap<>());
	}

	public Substitution(Substitution clone) {
		this(new TreeMap<>(clone.substitution));
	}
	
	static Substitution unify(Literal literal, Instance instance, Substitution substitution) {
		return unify(literal.getAtom(), instance, substitution);
	}

	/**
	 * Computes the unifier of the atom and the instance and stores it in the variable substitution.
	 * @param atom the body atom to unify
	 * @param instance the ground instance
	 * @param substitution if the atom does not unify, this is left unchanged.
	 * @return true if the atom and the instance unify. False otherwise
	 */
	static Substitution unify(Atom atom, Instance instance, Substitution substitution) {
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
	 * Checks if the left possible non-ground term unifies with the ground term.
	 * @param termNonGround
	 * @param termGround
	 * @return
	 */
	public boolean unifyTerms(Term termNonGround, Term termGround) {
		if (termNonGround == termGround) {
			// Both terms are either the same constant or the same variable term
			return true;
		} else if (termNonGround instanceof ConstantTerm) {
			// Since right term is ground, both terms differ
			return false;
		} else if (termNonGround instanceof VariableTerm) {
			VariableTerm variableTerm = (VariableTerm)termNonGround;
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
		// Note: We're destroying type information here.
		return substitution.put(variableTerm, groundTerm);
	}

	public boolean isEmpty() {
		return substitution.isEmpty();
	}

	public <T extends Substitutable<T>> T applyTo(T substitutable) {
		return substitutable.substitute(this);
	}

	@SuppressWarnings("unchecked")
	public <T extends Substitutable<? super T>> List<T> applyTo(Collection<T> substitutables) {
		return substitutables.stream().map(s -> (T) s.substitute(this)).collect(Collectors.toList());
	}

	/**
	 * Prints the variable substitution in a uniform way (sorted by variable names).
	 *
	 * @return
	 */
	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		for (Map.Entry<VariableTerm, Term> e : substitution.entrySet()) {
			ret.append("_").append(e.getKey()).append(":").append(e.getValue());
		}
		return ret.toString();
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

		return substitution != null ? substitution.equals(that.substitution) : that.substitution == null;
	}

	@Override
	public int hashCode() {
		return substitution != null ? substitution.hashCode() : 0;
	}

	public static Substitution findEqualizingSubstitution(BasicAtom generalAtom, BasicAtom specificAtom) {
		// Some hard examples:
		// p(A,f(A)) with p(X,A) where variable occurs as subterm again and where some variable is shared!
		// First, rename all variables in the specific
		if (!generalAtom.getPredicate().equals(specificAtom.getPredicate())) {
			return null;
		}
		Substitution specializingSubstitution = new Substitution();
		String renamedVariablePrefix = "_Vrenamed_";	// Pick prefix guaranteed to not occur in generalAtom.
		for (int i = 0; i < generalAtom.getPredicate().getArity(); i++) {
			specializingSubstitution = specializeSubstitution(specializingSubstitution,
				generalAtom.getTerms().get(i),
				specificAtom.getTerms().get(i).renameVariables(renamedVariablePrefix));
			if (specializingSubstitution == null) {
				return null;
			}
		}
		return specializingSubstitution;
	}

	private static Substitution specializeSubstitution(Substitution substitution, Term generalTerm, Term specificTerm) {
		if (generalTerm == specificTerm) {
			return substitution;
		}
		// If the general term is a variable, check its current substitution and see whether this matches the specific term.
		if (generalTerm instanceof VariableTerm) {
			Term substitutedGeneralTerm = substitution.eval((VariableTerm) generalTerm);
			// If the variable is not bound already, bind it to the specific term.
			if (substitutedGeneralTerm == null) {
				substitution.put((VariableTerm) generalTerm, specificTerm);
				return substitution;
			}
			// The variable is bound, check whether its result is exactly the specific term.
			// Note: checking whether the bounded term is more general than the specific one would yield
			//       wrong results, e.g.: p(X,X) and p(f(A),f(g(B))) are incomparable, but f(A) is more general than f(g(B)).
			if (substitutedGeneralTerm != specificTerm) {
				return null;
			}
		}
		if (generalTerm instanceof FunctionTerm) {
			// Check if both given terms are function terms.
			if (!(specificTerm instanceof FunctionTerm)) {
				return null;
			}
			// Check that they are the same function.
			FunctionTerm fgeneralTerm = (FunctionTerm) generalTerm;
			FunctionTerm fspecificTerm = (FunctionTerm) specificTerm;
			if (fgeneralTerm.getSymbol() != fspecificTerm.getSymbol()
				|| fgeneralTerm.getTerms().size() != fspecificTerm.getTerms().size()) {
				return null;
			}
			// Check/specialize their subterms.
			for (int i = 0; i < fgeneralTerm.getTerms().size(); i++) {
				substitution = specializeSubstitution(substitution, fgeneralTerm, fspecificTerm);
				if (substitution == null) {
					return null;
				}
			}
		}
		if (generalTerm instanceof ConstantTerm) {
			// Equality was already checked above, so terms are different.
			return null;
		}
		throw new RuntimeException("Trying to specialize a term that is neither variable, constant, nor function. Should not happen");
	}
}
