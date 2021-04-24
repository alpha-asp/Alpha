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

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.ASPCore2Literal;
import at.ac.tuwien.kr.alpha.api.programs.transforms.ASPCore2RuleVisitor;
import at.ac.tuwien.kr.alpha.api.rules.ASPCore2Rule;
import at.ac.tuwien.kr.alpha.api.rules.BasicRule;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceRule;
import at.ac.tuwien.kr.alpha.api.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.rules.heads.NormalHeadImpl;
import at.ac.tuwien.kr.alpha.commons.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.programs.ASPCore2ProgramImpl;

/**
 * Copyright (c) 2017-2020, the Alpha Team.
 */
public class ChoiceHeadToNormal extends ProgramTransformation<ASPCore2Program, ASPCore2Program> implements ASPCore2RuleVisitor {

	private final static String PREDICATE_NEGATION_PREFIX = "_n";

	private ASPCore2ProgramImpl.Builder programBuilder = ASPCore2ProgramImpl.builder();

	@Override
	public ASPCore2Program apply(ASPCore2Program inputProgram) {
		programBuilder.addFacts(inputProgram.getFacts());
		programBuilder.addInlineDirectives(inputProgram.getInlineDirectives());
		for (ASPCore2Rule<? extends Head> rule : inputProgram.getRules()) {
			visit(rule);
		}
		return programBuilder.build();
	}

	private static boolean containsIntervalTerms(Atom atom) {
		for (Term term : atom.getTerms()) {
			if (IntervalTerm.termContainsIntervalTerm(term)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void visit(ASPCore2Rule<? extends Head> rule) {
		rule.accept(this);
	}


	@Override
	public void visit(ChoiceRule rule) {
		ChoiceHead choiceHead = rule.getHead();
		// Choice rules with boundaries are not yet supported.
		if (choiceHead.getLowerBound() != null || choiceHead.getUpperBound() != null) {
			throw new UnsupportedOperationException("Found choice rule with bounds, which are not yet supported. Rule is: " + rule);
		}
		for (ChoiceHead.ChoiceElement choiceElement : choiceHead.getChoiceElements()) {
			handleChoiceElement(rule, choiceElement);
		}
	}

	private void handleChoiceElement(ChoiceRule rule, ChoiceHead.ChoiceElement element) {
		// Create two guessing rules for each choiceElement.

		// Construct common body to both rules.
		Atom head = element.getChoiceAtom();
		List<ASPCore2Literal> ruleBody = new ArrayList<>(rule.getBody());
		ruleBody.addAll(element.getConditionLiterals());

		if (containsIntervalTerms(head)) {
			throw new RuntimeException("Program contains a choice rule with interval terms in its head. This is not supported (yet).");
		}

		// Construct head atom for the choice.
		Predicate headPredicate = head.getPredicate();

		Predicate negPredicate = Predicates.getPredicate(PREDICATE_NEGATION_PREFIX + headPredicate.getName(), headPredicate.getArity() + 1, true);
		List<Term> headTerms = new ArrayList<>(head.getTerms());
		headTerms.add(0, Terms.newConstant("1"));
		// FIXME: when introducing classical negation, this is 1 for classical positive atoms and 0 for
		// classical negative atoms.
		Atom negHead = Atoms.newBasicAtom(negPredicate, headTerms);

		// Construct two guessing rules.
		List<ASPCore2Literal> guessingRuleBodyWithNegHead = new ArrayList<>(ruleBody);
		guessingRuleBodyWithNegHead.add(Atoms.newBasicAtom(head.getPredicate(), head.getTerms()).toLiteral(false));
		programBuilder.addRule(Rules.newBasicRule(new NormalHeadImpl(negHead), guessingRuleBodyWithNegHead));

		List<ASPCore2Literal> guessingRuleBodyWithHead = new ArrayList<>(ruleBody);
		guessingRuleBodyWithHead.add(Atoms.newBasicAtom(negPredicate, headTerms).toLiteral(false));
		programBuilder.addRule(Rules.newBasicRule(new NormalHeadImpl(head), guessingRuleBodyWithHead));
		// TODO: when cardinality constraints are possible, process the boundaries by adding a constraint with a cardinality check.		
	}
	
	@Override
	public void visit(BasicRule rule) {
		// Basic rule has a NormalHead, we simply pass those through
		programBuilder.addRule(rule);
	}
}
