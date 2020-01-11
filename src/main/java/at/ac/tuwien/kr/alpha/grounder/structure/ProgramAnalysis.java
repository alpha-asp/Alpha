package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ProgramAnalysis {

	private final Map<Predicate, LinkedHashSet<NonGroundRule>> predicateDefiningRules;
	private Map<NonGroundRule, LinkedHashSet<NonGroundRule>> rulesDerivingSameHead;
	private Set<NonGroundRule> isFullyNonProjective;

	public ProgramAnalysis(Program program) {
		predicateDefiningRules = new LinkedHashMap<>();
		isFullyNonProjective = new HashSet<>();
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new LinkedHashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	/**
	 * Runs program analysis after all defining rules have been recorded.
	 */
	public void runProgramAnalysis() {
		computeRulesDerivingSameHeadBasedOnPredicate();
	}

	private void computeRulesDerivingSameHeadBasedOnPredicate() {
		rulesDerivingSameHead = new LinkedHashMap<>();
		// Iterate all rules having the same predicate in the head.
		boolean isCompletable;
		for (Map.Entry<Predicate, LinkedHashSet<NonGroundRule>> definingRules : predicateDefiningRules.entrySet()) {
			isCompletable = true;
			LinkedHashSet<NonGroundRule> rules = definingRules.getValue();
			for (NonGroundRule rule : rules) {
				if (!rule.isNonProjective() && !rule.isFunctionallyDependent()) {
					isCompletable = false;
				}
				rulesDerivingSameHead.put(rule, rules);
			}
			if (isCompletable) {
				this.isFullyNonProjective.addAll(rules);
			}
		}
	}

	public boolean isRuleFullyNonProjective(NonGroundRule nonGroundRule) {
		return isFullyNonProjective.contains(nonGroundRule);
	}

	public Map<NonGroundRule, LinkedHashSet<NonGroundRule>> getRulesDerivingSameHead() {
		return rulesDerivingSameHead;
	}
}
