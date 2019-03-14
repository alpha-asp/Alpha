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
package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.Program;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.impl.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.IntervalAtom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rewrites all interval terms in a rule into a new variable and an IntervalAtom.
 * Copyright (c) 2017-2018, the Alpha Team.
 */
public class IntervalTermToIntervalAtom implements ProgramTransformation {
	private static final String INTERVAL_VARIABLE_PREFIX = "_Interval";

	/**
	 * Rewrites intervals into a new variable and special IntervalAtom.
	 * @return true if some interval occurs in the rule.
	 */
	private static boolean rewriteIntervalSpecifications(BasicRule rule) {
		// Collect all intervals and replace them with variables.
		Map<VariableTerm, IntervalTerm> intervalReplacements = new HashMap<>();
		for (Literal literal : rule.getBody()) {
			rewriteAtom(literal.getAtom(), intervalReplacements);
		}
		if (rule.getHead() != null) {
			if (!rule.getHead().isNormal()) {
				throw new RuntimeException("Cannot rewrite intervals in rules whose head contains a disjunction or choice. Given rule is: " + rule);
			}
			rewriteAtom(((DisjunctiveHead)rule.getHead()).disjunctiveAtoms.get(0), intervalReplacements);
		}

		// Add new IntervalAtoms representing the interval specifications.
		for (Map.Entry<VariableTerm, IntervalTerm> interval : intervalReplacements.entrySet()) {
			rule.getBody().add(new IntervalAtom(interval.getValue(), interval.getKey()).toLiteral());
		}
		return !intervalReplacements.isEmpty();
	}

	/**
	 * Replaces every IntervalTerm by a new variable and returns a mapping of the replaced VariableTerm -> IntervalTerm.
	 */
	private static void rewriteAtom(Atom atom, Map<VariableTerm, IntervalTerm> intervalReplacement) {
		List<Term> termList = atom.getTerms();
		for (int i = 0; i < termList.size(); i++) {
			Term term = termList.get(i);
			if (term instanceof IntervalTerm) {
				VariableTerm replacementVariable = VariableTerm.getInstance(INTERVAL_VARIABLE_PREFIX + intervalReplacement.size());
				intervalReplacement.put(replacementVariable, (IntervalTerm) term);
				termList.set(i, replacementVariable);
			}
			if (term instanceof FunctionTerm) {
				// Rewrite function terms recursively.
				FunctionTerm rewrittenFunctionTerm = rewriteFunctionTerm((FunctionTerm) term, intervalReplacement);
				termList.set(i, rewrittenFunctionTerm);
			}
		}
	}

	private static FunctionTerm rewriteFunctionTerm(FunctionTerm functionTerm, Map<VariableTerm, IntervalTerm> intervalReplacement) {
		List<Term> termList = new ArrayList<>(functionTerm.getTerms());
		boolean didChange = false;
		for (int i = 0; i < termList.size(); i++) {
			Term term = termList.get(i);
			if (term instanceof IntervalTerm) {
				VariableTerm replacementVariable = VariableTerm.getInstance("_Interval" + intervalReplacement.size());
				intervalReplacement.put(replacementVariable, (IntervalTerm) term);
				termList.set(i, replacementVariable);
				didChange = true;
			}
			if (term instanceof FunctionTerm) {
				// Recursively rewrite function terms.
				FunctionTerm rewrittenFunctionTerm = rewriteFunctionTerm((FunctionTerm) term, intervalReplacement);
				if (rewrittenFunctionTerm != term) {
					termList.set(i, rewrittenFunctionTerm);
					didChange = true;
				}
			}
		}
		if (didChange) {
			return FunctionTerm.getInstance(functionTerm.getSymbol(), termList);
		}
		return functionTerm;
	}

	@Override
	public void transform(Program inputProgram) {
		for (BasicRule rule : inputProgram.getRules()) {
			rewriteIntervalSpecifications(rule);
		}
	}
}
