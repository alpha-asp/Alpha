/**
 * Copyright (c) 2017-2018, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains a potentially negated {@link BasicAtom}.
 * 
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class BasicLiteral extends Literal {
	
	public BasicLiteral(BasicAtom atom, boolean negated) {
		super(atom, negated);
	}
	
	@Override
	public BasicAtom getAtom() {
		return (BasicAtom)atom;
	}

	/**
	 * Returns a new copy of this literal whose {@link Literal#isNegated()} status is inverted
	 */
	@Override
	public BasicLiteral negate() {
		return new BasicLiteral(getAtom(), !negated);
	}

	/**
	 * @see Atom#substitute(Substitution)
	 */
	@Override
	public BasicLiteral substitute(Substitution substitution) {
		return new BasicLiteral(getAtom().substitute(substitution), negated);
	}

	/**
	 * Set of all variables occurring in the Atom that are potentially binding, i.e., variables in positive atoms.
	 * 
	 * @return
	 */
	@Override
	public Set<VariableTerm> getBindingVariables() {
		if (negated) {
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
	 * @return
	 */
	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		if (!negated) {
			// Positive literal has only binding variables.
			return Collections.emptySet();
		}
		Set<VariableTerm> nonbindingVariables = new HashSet<>();
		for (Term term : atom.getTerms()) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}
		return nonbindingVariables;
	}
}
