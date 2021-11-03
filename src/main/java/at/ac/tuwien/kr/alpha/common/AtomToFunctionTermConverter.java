/*
 *  Copyright (c) 2021 Siemens AG
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *  2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Converts {@link BasicAtom}s to {@link FunctionTerm}s and back
 * (which is needed if the atom is to be nested in another term).
 */
public class AtomToFunctionTermConverter {

	/**
	 * A map of known predicates.
	 * Keys are predicates with default invisible properties.
	 * Values are predicates with "real" invisible properties (as first seen).
	 */
	private static final Map<Predicate, Predicate> PREDICATES = new HashMap<>();

	/**
	 * Converts the given atom to a function term.
	 * The {@link BasicAtom#getPredicate()} is stored so that, when the atom is converted back by {@link #toAtom(FunctionTerm)},
	 * invisible properties such as {@link Predicate#isInternal()} are restored.
	 *
	 * @param atom the atom to convert
	 * @return a representation of the visible properties of the atom as a function term
	 */
	public static FunctionTerm toFunctionTerm(BasicAtom atom) {
		rememberPredicate(atom.getPredicate());
		return FunctionTerm.getInstance(atom.getPredicate().getName(), new ArrayList<>(atom.getTerms()));
	}

	/**
	 * Converts the given function term back to an atom.
	 * The {@link BasicAtom#getPredicate()} is recovered as it was saved by {@link #toFunctionTerm(BasicAtom)}.
	 *
	 * @param functionTerm the function term to convert
	 * @return the result of translating the function term back to a basic atom
	 */
	public static BasicAtom toAtom(FunctionTerm functionTerm) {
		final String symbol = functionTerm.getSymbol();
		final List<Term> terms = functionTerm.getTerms();
		final int arity = terms.size();
		return new BasicAtom(recallPredicate(symbol, arity), terms);
	}

	private static void rememberPredicate(Predicate value) {
		final Predicate key = Predicate.getInstance(value.getName(), value.getArity());
		final Predicate existingValue = PREDICATES.get(key);
		if (existingValue == null) {
			PREDICATES.put(key, value);
		} else if (!existingValue.equals(value)) {
			throw new IllegalArgumentException("Predicate is used with two different sets of invisible properties: " + value);
		}
	}

	private static Predicate recallPredicate(String symbol, int arity) {
		final Predicate key = Predicate.getInstance(symbol, arity);
		final Predicate value = PREDICATES.get(key);
		if (value == null) {
			throw new IllegalArgumentException("Unknown predicate: " + key);
		}
		return value;
	}

}
