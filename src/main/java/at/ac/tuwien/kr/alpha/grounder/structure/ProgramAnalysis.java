package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ProgramAnalysis {

	private final Map<Predicate, LinkedHashSet<NonGroundRule>> predicateDefiningRules;
	private Map<NonGroundRule, LinkedHashSet<NonGroundRule>> rulesDerivingSameHead;
	private Map<NonGroundRule, Boolean> isFullyNonProjective;
	private final Map<BasicAtom, LinkedHashSet<RuleAtom>> completionBodiesPerHead;

	public ProgramAnalysis(Program program) {
		predicateDefiningRules = new LinkedHashMap<>();
		completionBodiesPerHead = new LinkedHashMap<>();
		isFullyNonProjective = new LinkedHashMap<>();
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new LinkedHashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	private void computeRulesDerivingSameHeadBasedOnPredicate() {
		rulesDerivingSameHead = new LinkedHashMap<>();
		// Iterate all rules having the same predicate in the head.
		boolean isFullyNonProjective;
		for (Map.Entry<Predicate, LinkedHashSet<NonGroundRule>> definingRules : predicateDefiningRules.entrySet()) {
			isFullyNonProjective = true;
			LinkedHashSet<NonGroundRule> rules = definingRules.getValue();
			for (NonGroundRule rule : rules) {
				if (!rule.isNonProjective()) {
					isFullyNonProjective = false;
				}
				rulesDerivingSameHead.put(rule, rules);
			}
			for (NonGroundRule rule : rules) {
				this.isFullyNonProjective.put(rule, isFullyNonProjective);
			}
		}
	}

	public boolean isRuleFullyNonProjective(NonGroundRule nonGroundRule) {
		return isFullyNonProjective.get(nonGroundRule);
	}

	public Map<NonGroundRule, LinkedHashSet<NonGroundRule>> getRulesDerivingSameHead() {
		if (rulesDerivingSameHead == null) {
			computeRulesDerivingSameHeadBasedOnPredicate();
		}
		return rulesDerivingSameHead;
	}

	private void computeCompletionSameHeads() {
		// TODO: error: completionBodiesPerHead assumes that RuleAtoms can be constructed (may not be possible for non-ground rules?)
		// Solution: use NonGroundRules instead of RuleAtoms for grouping.

		// TODO: compute which rule's heads may derive the same atoms.
		// Note: this can be done at various levels of detail/analysis to strike the right balance between useful distinction and computational overhead.

		// TODO: for the moment we only consider the predicate (i.e., same predicate -> same head atoms).
/*		for (Map.Entry<Predicate, LinkedHashSet<NonGroundRule>> definingRules : predicateDefiningRules.entrySet()) {
			Predicate predicate = definingRules.getKey();
			HashSet<NonGroundRule> rules = definingRules.getValue();
			LinkedHashSet<RuleAtom> definingBodyRepresentingAtoms = new LinkedHashSet<>(rules.size());
			for (NonGroundRule rule : rules) {
				// TODO: construct rule representing atom.
				definingBodyRepresentingAtoms.add(rule.)
			}
			for (NonGroundRule nonGroundRule : rules) {
				completionBodiesPerHead.put(nonGroundRule.getHeadAtom(), rules);
			}

		}
 */
	}

	public Collection<RuleAtom> getCompletionBodies(BasicAtom ruleHeadWithNormalizedVariables) {
		// TODO: we could compute completions not for predicates but for the same partially-ground head atoms.
		// TODO: Example: p(a,Y) :- q(Y). and p(b,Y) :- r(X,Y,Z). is fine for p(a,_).
		// TODO: But: p(a,Y) :- q(Y). and p(X,Y) :- r(X,Y,Z). is not fine for p(a,_).
		// TODO: in order to work on partially-instantiated atoms, we have to check whether rule heads unify (or one is more general than the other).
		return Collections.unmodifiableSet(completionBodiesPerHead.get(ruleHeadWithNormalizedVariables));
	}

	public void computeCompletionBodies() {
		for (Map.Entry<Predicate, LinkedHashSet<NonGroundRule>> predicateDefiningRules : predicateDefiningRules.entrySet()) {
			Predicate predicate = predicateDefiningRules.getKey();
			HashSet<NonGroundRule> definingRules = predicateDefiningRules.getValue();
			// TODO: iterate all defining rules, identify partially-ground rule heads where all rules generating instances for that same rule are non-projective.
			HashMap<BasicAtom, HashSet<NonGroundRule>> unifyingRuleHeads = new LinkedHashMap<>();
			for (NonGroundRule definingRule : definingRules) {
				if (!definingRule.isNonProjective()) {

				}
			}
		}
	}
}
