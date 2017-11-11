package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.symbols.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ChoiceHeadToNormal implements ProgramTransformation {
	private final static String PREDICATE_NEGATION_PREFIX = "_n";

	@Override
	public void transform(Program inputProgram) {
		List<Rule> additionalRules = new ArrayList<>();

		Iterator<Rule> ruleIterator = inputProgram.getRules().iterator();
		while (ruleIterator.hasNext()) {
			Rule rule = ruleIterator.next();

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
				throw new RuntimeException("Found choice rule with bounds, which are not yet supported. Rule is: " + rule);
			}

			// Only rewrite rules with a choice in their head.
			for (ChoiceHead.ChoiceElement choiceElement : choiceHead.getChoiceElements()) {
				// Create two guessing rules for each choiceElement.

				// Construct common body to both rules.
				Atom head = choiceElement.choiceAtom;
				List<Literal> ruleBody = new ArrayList<>(rule.getBody());
				ruleBody.addAll(choiceElement.conditionLiterals);

				if (containsIntervalTerms(head)) {
					throw new RuntimeException("Program contains a choice rule with interval terms in its head. This is not supported (yet).");
				}

				// Construct head atom for the guess.
				Predicate headPredicate = head.getPredicate();
				Predicate negPredicate = Predicate.getInstance(PREDICATE_NEGATION_PREFIX + headPredicate.getSymbol(), headPredicate.getRank() + 1, true);
				List<Term> headTerms = new ArrayList<>(head.getTerms());
				headTerms.add(0, Constant.getInstance("1"));	// FIXME: when introducing classical negation, this is 1 for classical positive atoms and 0 for classical negative atoms.
				Atom negHead = new BasicAtom(negPredicate, headTerms);

				// Construct two guessing rules.
				List<Literal> guessingRuleBodyWithNegHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithNegHead.add(new BasicAtom(head, true));
				additionalRules.add(new Rule(new DisjunctiveHead(Collections.singletonList(negHead)), guessingRuleBodyWithNegHead));

				List<Literal> guessingRuleBodyWithHead = new ArrayList<>(ruleBody);
				guessingRuleBodyWithHead.add(new BasicAtom(negHead, true));
				additionalRules.add(new Rule(new DisjunctiveHead(Collections.singletonList(head)), guessingRuleBodyWithHead));

				// TODO: when cardinality constraints are possible, process the boundaries by adding a constraint with a cardinality check.
			}
		}
		inputProgram.getRules().addAll(additionalRules);
	}

	private static boolean containsIntervalTerms(Atom atom) {
		for (Term term : atom.getTerms()) {
			if (IntervalTerm.termContainsIntervalTerm(term)) {
				return true;
			}
		}
		return false;
	}
}
