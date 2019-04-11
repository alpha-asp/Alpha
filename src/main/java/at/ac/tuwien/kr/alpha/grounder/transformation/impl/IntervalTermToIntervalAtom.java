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
package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.impl.NormalProgram;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.NormalHead;
import at.ac.tuwien.kr.alpha.common.rule.impl.NormalRule;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.IntervalAtom;
import at.ac.tuwien.kr.alpha.grounder.transformation.ProgramTransformation;

/**
 * Rewrites all interval terms in a rule into a new variable and an IntervalAtom. Copyright (c) 2017-2018, the Alpha Team.
 */
public class IntervalTermToIntervalAtom extends ProgramTransformation<NormalProgram, NormalProgram> {
	private static final String INTERVAL_VARIABLE_PREFIX = "_Interval";

	/**
	 * Rewrites intervals into a new variable and special IntervalAtom.
	 * 
	 * @return true if some interval occurs in the rule.
	 */
	private static NormalRule rewriteIntervalSpecifications(NormalRule rule) {
		// Collect all intervals and replace them with variables.
		Map<VariableTerm, IntervalTerm> intervalReplacements = new HashMap<>();

		List<Literal> rewrittenBody = new ArrayList<>();
		NormalHead rewrittenHead = rule.isConstraint() ? null : new NormalHead(rule.getHeadAtom());

		for (Literal literal : rule.getBody()) {
			rewrittenBody.add(rewriteLiteral(literal, intervalReplacements));
		}
		if (rewrittenHead != null) {
			rewrittenHead = new NormalHead(rewriteLiteral(rule.getHeadAtom().toLiteral(), intervalReplacements).getAtom());
		}

		// Add new IntervalAtoms representing the interval specifications.
		for (Map.Entry<VariableTerm, IntervalTerm> interval : intervalReplacements.entrySet()) {
			rewrittenBody.add(new IntervalAtom(interval.getValue(), interval.getKey()).toLiteral());
		}
		return new NormalRule(rewrittenHead, rewrittenBody);
	}

	/**
	 * Replaces every IntervalTerm by a new variable and returns a mapping of the replaced VariableTerm -> IntervalTerm.
	 */
	private static Literal rewriteLiteral(Literal lit, Map<VariableTerm, IntervalTerm> intervalReplacement) {
		Atom atom = lit.getAtom();
		List<Term> termList = new ArrayList<>(atom.getTerms());
		boolean didChange = false;
		for (int i = 0; i < termList.size(); i++) {
			Term term = termList.get(i);
			if (term instanceof IntervalTerm) {
				VariableTerm replacementVariable = VariableTerm.getInstance(INTERVAL_VARIABLE_PREFIX + intervalReplacement.size());
				intervalReplacement.put(replacementVariable, (IntervalTerm) term);
				termList.set(i, replacementVariable);
				didChange = true;
			}
			if (term instanceof FunctionTerm) {
				// Rewrite function terms recursively.
				FunctionTerm rewrittenFunctionTerm = rewriteFunctionTerm((FunctionTerm) term, intervalReplacement);
				termList.set(i, rewrittenFunctionTerm);
				didChange = true;
			}
		}
		if (didChange) {
			Atom rewrittenAtom = atom.setTerms(termList);
			return lit.isNegated() ? rewrittenAtom.toLiteral().negate() : rewrittenAtom.toLiteral();
		}
		return lit;
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
	public NormalProgram apply(NormalProgram inputProgram) {
		List<NormalRule> rewrittenRules = new ArrayList<>();
		for (NormalRule rule : inputProgram.getRules()) {
			rewrittenRules.add(rewriteIntervalSpecifications(rule));
		}
		return new NormalProgram(rewrittenRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}
}
