/*
 * Copyright (c) 2017-2018, 2020, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Substitution;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.apache.commons.collections4.SetUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A potentially negated {@link Atom}
 */
public abstract class Literal implements Comparable<Literal> {
	
	protected final Atom atom;
	protected final boolean positive;
	
	public Literal(Atom atom, boolean positive) {
		this.atom = atom;
		this.positive = positive;
	}

	public Atom getAtom() {
		return atom;
	}

	public boolean isNegated() {
		return !positive;
	}
	
	public abstract Literal negate();
	
	public abstract Literal substitute(Substitution substitution);

	/**
	 * Set of all variables occurring in the Atom that are potentially binding, i.e., variables in positive atoms.
	 *
	 * This is the default implementation used by {@link BasicLiteral} and {@link at.ac.tuwien.kr.alpha.grounder.atoms.BodyRepresentingLiteral}
	 * and may be overridden in implementing classes.
	 *
	 * @return the set of binding variables in this literal
	 */
	public Set<VariableTerm> getBindingVariables() {
		if (!positive) {
			// Negative literal has no binding variables.
			return Collections.emptySet();
		}
		Set<VariableTerm> bindingVariables = new HashSet<>();
		for (Term term : atom.getTerms()) {
			bindingVariables.addAll(term.getOccurringVariables());
		}
		return bindingVariables;
	}

	/**
	 * Set of all variables occurring in the Atom that are never binding, not even in positive atoms, e.g., variables in intervals or built-in atoms.
	 *
	 * This is the default implementation used by {@link BasicLiteral} and {@link at.ac.tuwien.kr.alpha.grounder.atoms.BodyRepresentingLiteral}
	 * and may be overridden in implementing classes.
	 *
	 * @return the set of non-binding variables in this literal
	 */
	public Set<VariableTerm> getNonBindingVariables() {
		if (positive) {
			// Positive literal has only binding variables.
			return Collections.emptySet();
		}
		Set<VariableTerm> nonbindingVariables = new HashSet<>();
		for (Term term : atom.getTerms()) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}
		return nonbindingVariables;
	}
	
	/**
	 * Union of {@link #getBindingVariables()} and {@link #getNonBindingVariables()}
	 */
	public Set<VariableTerm> getOccurringVariables() {
		return SetUtils.union(getBindingVariables(), getNonBindingVariables());
	}

	/**
	 * @see Atom#getPredicate()
	 */
	public Predicate getPredicate() {
		return atom.getPredicate();
	}

	/**
	 * @see Atom#getTerms()
	 */
	public List<Term> getTerms() {
		return atom.getTerms();
	}

	/**
	 * @see Atom#isGround()
	 */
	public boolean isGround() {
		return atom.isGround();
	}
	
	@Override
	public String toString() {
		return (positive ? "" : "not ") + atom.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Literal that = (Literal) o;

		return atom.equals(that.atom) && positive == that.positive;
	}

	@Override
	public int hashCode() {
		return 12 * atom.hashCode() + (positive ? 1 : 0);
	}

	@Override
	public int compareTo(Literal o) {
		if (this.positive == o.positive) {
			return this.getAtom().compareTo(o.getAtom());
		}
		return Boolean.compare(this.positive, o.positive);
	}
}
