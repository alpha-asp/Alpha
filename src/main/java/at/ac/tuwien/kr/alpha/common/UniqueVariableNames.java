/*
 *  Copyright (c) 2020-2022 Siemens AG
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Unifier;

public class UniqueVariableNames {

	private final Map<VariableTerm, Integer> variablesToOccurrences = new HashMap<>();

	/**
	 * This method standardises apart a heuristic directive and a rule.
	 * Computes a unifier to rename variables in a {@link BasicRule} so that they are different from all variables
	 * occurring in a given {@link HeuristicDirective}. All variable names in the directive stay the same, and variable
	 * names in the rule stay the same if possible.
	 * @param heuristicDirective a heuristic directive
	 * @param rule a rule
	 * @return a unifier to be applied to the rule.
	 */
	public Unifier makeVariableNamesUnique(HeuristicDirective heuristicDirective, BasicRule rule) {
		for (VariableTerm variable : heuristicDirective.getOccurringVariables()) {
			variablesToOccurrences.put(variable, 0);
		}
		return renameVariablesIfAlreadyUsed(rule.getOccurringVariables());
	}

	private Unifier renameVariablesIfAlreadyUsed(Set<VariableTerm> variables) {
		final Unifier unifier = new Unifier();
		for (VariableTerm variable : variables) {
			if (variablesToOccurrences.containsKey(variable)) {
				VariableTerm newVariable;
				do {
					newVariable = VariableTerm.getInstance(variable.toString() + "_" + (variablesToOccurrences.computeIfPresent(variable, (v, o) -> o + 1)));
				} while (variablesToOccurrences.containsKey(newVariable));
				unifier.put(variable, newVariable);
			} else {
				variablesToOccurrences.put(variable, 0);
			}
		}
		return unifier;
	}

}
