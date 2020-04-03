/*
 *  Copyright (c) 2020 Siemens AG
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

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UniqueVariableNames {

	private final Map<VariableTerm, Integer> variablesToOccurrences = new HashMap<>();

	public NonGroundNoGood makeVariableNamesUnique(NonGroundNoGood noGood) {
		final Unifier unifier = renameVariablesIfAlreadyUsed(noGood.getOccurringVariables());
		if (unifier.isEmpty()) {
			return noGood;
		}
		final List<Literal> newLiterals = new ArrayList<>(noGood.size());
		for (Literal literal : noGood) {
			newLiterals.add(literal.substitute(unifier));
		}
		return new NonGroundNoGood(noGood.getType(), newLiterals, noGood.hasHead());

	}

	private Unifier renameVariablesIfAlreadyUsed(Set<VariableTerm> variables) {
		final Unifier unifier = new Unifier();
		for (VariableTerm variable : variables) {
			if (variablesToOccurrences.containsKey(variable)) {
				VariableTerm newVariable;
				do {
					newVariable = VariableTerm.getInstance(variable.toString() + "_" + (variablesToOccurrences.computeIfPresent(variable, (v,o) -> o+1)));
				} while (variablesToOccurrences.containsKey(newVariable));
				unifier.put(variable, newVariable);
			} else {
				variablesToOccurrences.put(variable, 0);
			}
		}
		return unifier;
	}

}
