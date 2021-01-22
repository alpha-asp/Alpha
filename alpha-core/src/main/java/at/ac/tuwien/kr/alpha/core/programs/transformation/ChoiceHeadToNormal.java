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
package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.program.*;
import at.ac.tuwien.kr.alpha.api.terms.*;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;
import at.ac.tuwien.kr.alpha.core.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.core.rules.heads.Head;
import at.ac.tuwien.kr.alpha.core.rules.heads.NormalHeadImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2017-2020, the Alpha Team.
 */
public class ChoiceHeadToNormal extends ProgramTransformation<InputProgramImpl, InputProgramImpl> {
	private final static String PREDICATE_NEGATION_PREFIX = "_n";

	@Override
	public InputProgramImpl apply(InputProgramImpl inputProgram) {
		InputProgramImpl.Builder programBuilder = InputProgramImpl.builder();
		List<BasicRule> additionalRules = new ArrayList<>();

		List<BasicRule> srcRules = new ArrayList<>(inputProgram.getRules());
		Iterator<BasicRule> ruleIterator = srcRules.iterator();
		while (ruleIterator.hasNext()) {
			BasicRule rule = ruleIterator.next();

			Head ruleHead = rule.getHead();
			if (!(ruleHead instanceof ChoiceHead)) {
				// Rule is constraint or without choice in the head. Leave as is.
				continue;
			}

			// Remove this rule, as it will be transformed.
			ruleIterator.remove();

			ChoiceHead choiceHead = (ChoiceHead) ruleHead;
			// Choice rules with boundaries are not yet supported.
			if (choiceHead.getLowerBound() != null || choiceHead.getUpperBound() != null) {
				throw new UnsupportedOperationException("Found choice rule with bounds, which are not yet supported. Rule is: " + rule);
			}

			// Only rewrite rules with a choice in their head.
			for (ChoiceHead.ChoiceElement choiceElement : choiceHead.getChoiceElements()) {
				// Create two guessing rules for each choiceElement.

				// Construct common body to both rules.
				CoreAtom head = choiceElement.choiceAtom;
				List<CoreLiteral> ruleBody = new ArrayList<>(rule.getBody());
				ruleBody.addAll(choiceElement.conditionLiterals);

				if (containsIntervalTerms(head)) {
					throw new RuntimeException("Program contains a choice rule with interval terms in its head. This is not supported (yet).");
				}

				// Construct head atom for the choice.
				CorePredicate headPredicate = head.getPredicate();

				CorePredicate negPredicate = CorePredicate.getInstance(PREDICATE_NEGATION_PREFIX + headPredicate.getName(), headPredicate.getArity() + 1, true);
				List<CoreTerm> headTerms = new ArrayList<>(head.getTerms());
				headTerms.add(0, CoreConstantTerm.getInstance("1")); // FIXME: when introducing classical negation, this is 1 for classical positive atoms and 0 for
																	// classical negative atoms.
				CoreAtom negHead = new BasicAtom(negPredicate, headTerms);

				// Construct two guessing rules.
				List<CoreLiteral> guessingRuleBodyWithNegHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithNegHead.add(new BasicAtom(head.getPredicate(), head.getTerms()).toLiteral(false));
				additionalRules.add(new BasicRule(new NormalHeadImpl(negHead), guessingRuleBodyWithNegHead));

				List<CoreLiteral> guessingRuleBodyWithHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithHead.add(new BasicAtom(negPredicate, headTerms).toLiteral(false));
				additionalRules.add(new BasicRule(new NormalHeadImpl(head), guessingRuleBodyWithHead));

				// TODO: when cardinality constraints are possible, process the boundaries by adding a constraint with a cardinality check.
			}
		}
		return programBuilder.addRules(srcRules).addRules(additionalRules).addFacts(inputProgram.getFacts())
				.addInlineDirectives(inputProgram.getInlineDirectives()).build();
	}

	private static boolean containsIntervalTerms(CoreAtom atom) {
		for (CoreTerm term : atom.getTerms()) {
			if (IntervalTerm.termContainsIntervalTerm(term)) {
				return true;
			}
		}
		return false;
	}
}
