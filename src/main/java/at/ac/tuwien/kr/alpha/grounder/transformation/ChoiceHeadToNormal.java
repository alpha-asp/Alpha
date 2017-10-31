package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Head;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ChoiceHeadToNormal implements ProgramTransformation {
	public final static String PREDICATE_NEGATION_PREFIX = "_n";
	private final NaiveGrounder grounder;

	public ChoiceHeadToNormal(NaiveGrounder grounder) {
		this.grounder = grounder;
	}

	@Override
	public void transform(Program inputProgram) {
		List<Rule> additionalRules = new ArrayList<>();

		Iterator<Rule> ruleIterator = inputProgram.getRules().iterator();
		while (ruleIterator.hasNext()) {
			Rule rule = ruleIterator.next();

			Head ruleHead = rule.getHead();
			if (ruleHead == null || ruleHead.choiceHead == null) {
				// Rule is constraint or without choice in the head. Leave as is.
				continue;
			}
			ruleIterator.remove();
			// Choice rules with boundaries are not yet supported.
			if (ruleHead.choiceHead.getLowerBound() != null || ruleHead.choiceHead.getUpperBound() != null) {
				throw new RuntimeException("Found choice rule with bounds, which are not yet supported. Rule is: " + rule);
			}

			// Only rewrite rules with a choice in their head.
			List<Head.ChoiceHead.ChoiceElement> choiceElements = ruleHead.choiceHead.getChoiceElements();
			for (Head.ChoiceHead.ChoiceElement choiceElement : choiceElements) {
				// Create two guessing rules for each choiceElement.

				// Construct common body to both rules.
				BasicAtom head = choiceElement.choiceAtom;
				List<Literal> ruleBody = new ArrayList<>(rule.getBody());
				ruleBody.addAll(choiceElement.conditionLiterals);

				if (head.containsIntervalTerms()) {
					throw new RuntimeException("Program contains a choice rule with interval terms in its head. This is not supported (yet).");
				}

				// Construct head atom for the guess.
				Predicate headPredicate = head.getPredicate();
				Predicate negPredicate = new Predicate(PREDICATE_NEGATION_PREFIX + headPredicate.getPredicateName(), headPredicate.getArity() + 1);
				grounder.flagPredicateAsInternal(negPredicate);
				List<Term> headTerms = new ArrayList<>(head.getTerms());
				headTerms.add(0, ConstantTerm.getInstance("1"));	// FIXME: when introducing classical negation, this is 1 for classical positive atoms and 0 for classical negative atoms.
				BasicAtom negHead = new BasicAtom(negPredicate, headTerms);

				// Construct two guessing rules.
				List<Literal> guessingRuleBodyWithNegHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithNegHead.add(new BasicAtom(head, true));
				additionalRules.add(new Rule(Head.constructDisjunctiveHead(Collections.singletonList(negHead)), guessingRuleBodyWithNegHead));

				List<Literal> guessingRuleBodyWithHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithHead.add(new BasicAtom(negHead, true));
				additionalRules.add(new Rule(Head.constructDisjunctiveHead(Collections.singletonList(head)), guessingRuleBodyWithHead));

				// TODO: when cardinality constraints are possible, process the boundaries by adding a constraint with a cardinality check.
			}
		}
		inputProgram.getRules().addAll(additionalRules);
	}
}
