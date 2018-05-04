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
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Substitutable;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public interface Atom extends Comparable<Atom>, Substitutable<Atom> {
	
	Predicate getPredicate();
	List<Term> getTerms();

	boolean isGround();

	/**
	 * Set of all variables occurring in the Atom that are potentially binding
	 * @return
	 */
	default Set<VariableTerm> getBindingVariables() {
		return toLiteral().getBindingVariables();
	}

	/**
	 * Set of all variables occurring in the Atom that are never binding, not even in positive atoms, e.g., variables in intervals or built-in atoms.
	 * @return
	 */
	default Set<VariableTerm> getNonBindingVariables() {
		return toLiteral().getNonBindingVariables();
	}

	/**
	 * Collection of all variables occuring in the Atom
	 */
	default Set<VariableTerm> getOccurringVariables() {
		return getTerms().stream().flatMap(t -> t.getOccurringVariables().stream()).collect(Collectors.toSet());
	}

	/**
	 * This method applies a substitution to a potentially non-substitute atom.
	 * The resulting atom may be non-substitute.
	 * @param substitution the variable substitution to apply.
	 * @return the atom resulting from the applying the substitution.
	 */
	Atom substitute(Substitution substitution);
	
	/**
	 * Creates a non-negated literal containing this atom
	 */
	default Literal toLiteral() {
		return toLiteral(true);
	}
	
	/**
	 * Creates a literal containing this atom which will be negated if {@code positive} is {@code false}
	 * @param positive
	 * @return
	 */
	Literal toLiteral(boolean positive);

	@Override
	default int compareTo(Atom o) {
		if (o == null) {
			return 1;
		}

		final List<Term> aTerms = this.getTerms();
		final List<Term> bTerms = o.getTerms();

		if (aTerms.size() != bTerms.size()) {
			return Integer.compare(aTerms.size(), bTerms.size());
		}

		int result = this.getPredicate().compareTo(o.getPredicate());

		if (result != 0) {
			return result;
		}

		for (int i = 0; i < aTerms.size(); i++) {
			result = aTerms.get(i).compareTo(o.getTerms().get(i));
			if (result != 0) {
				return result;
			}
		}

		return 0;
	}
}
