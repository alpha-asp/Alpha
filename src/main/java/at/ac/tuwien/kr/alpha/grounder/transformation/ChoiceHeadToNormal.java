package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ChoiceHead;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.atoms.HiddenAtom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ChoiceHeadToNormal implements ProgramTransformation {
	public final static String PREDICATE_NEGATION_PREFIX = "_n";

	@Override
	public void transform(Program inputProgram) {
		List<Rule> additionalRules = new ArrayList<>();

		Iterator<Rule> ruleIterator = inputProgram.getRules().iterator();
		while (ruleIterator.hasNext()) {
			Rule rule = ruleIterator.next();

			if (rule.getHead() == null) {
				continue;
			}
			if (!(rule.getHead() instanceof ChoiceHead)) {
				continue;
			}
			ruleIterator.remove();
			// Choice rules with boundaries are not yet supported.
			if (((ChoiceHead) rule.getHead()).getLowerBound() != null || ((ChoiceHead) rule.getHead()).getUpperBound() != null) {
				throw new RuntimeException("Found choice rule with bounds, which are not yet supported. Rule is: " + rule);
			}

			// Only rewrite rules with a choice in their head.
			List<ChoiceHead.ChoiceElement> choiceElements = ((ChoiceHead) rule.getHead()).getChoiceElements();
			for (ChoiceHead.ChoiceElement choiceElement : choiceElements) {
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
				List<Term> headTerms = new ArrayList<>(head.getTerms());
				headTerms.add(0, ConstantTerm.getInstance("1"));	// FIXME: when introducing classical negation, this is 1 for classical positive atoms and 0 for classical negative atoms.
				HiddenAtom negHead = new HiddenAtom(negPredicate, headTerms);

				// Construct two guessing rules.
				List<Literal> guessingRuleBodyWithNegHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithNegHead.add(new BasicAtom(head, true));
				additionalRules.add(new Rule(negHead, guessingRuleBodyWithNegHead));

				List<Literal> guessingRuleBodyWithHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithHead.add(new HiddenAtom(negHead, true));
				additionalRules.add(new Rule(head, guessingRuleBodyWithHead));

				// TODO: when cardinality constraints are possible, process the boundaries by adding a constraint with a cardinality check.
			}
		}
		inputProgram.getRules().addAll(additionalRules);
	}
}
