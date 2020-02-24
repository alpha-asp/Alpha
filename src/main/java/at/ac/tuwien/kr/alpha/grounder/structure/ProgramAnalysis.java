package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static at.ac.tuwien.kr.alpha.grounder.Substitution.unify;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ProgramAnalysis {

	private final Map<Predicate, LinkedHashSet<NonGroundRule>> predicateDefiningRules;
	private TreeMap<Atom, Set<NonGroundRule>> rulesUnifyingWithHead = new TreeMap<>();
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
		// Iterate all rules having the same predicate in the head.
		boolean isCompletable;
		for (Map.Entry<Predicate, LinkedHashSet<NonGroundRule>> definingRules : predicateDefiningRules.entrySet()) {
			isCompletable = true;
			LinkedHashSet<NonGroundRule> rules = definingRules.getValue();
			for (NonGroundRule rule : rules) {
				if (!rule.isNonProjective() && !rule.isFunctionallyDependent()) {
					isCompletable = false;
				}
			}
			if (isCompletable) {
				this.isFullyNonProjective.addAll(rules);
			}
		}
	}

	public boolean isRuleFullyNonProjective(NonGroundRule nonGroundRule) {
		return isFullyNonProjective.contains(nonGroundRule);
	}

	public Set<NonGroundRule> getRulesUnifyingWithGroundHead(Atom groundHeadAtom) {
		// Check if we already computed all rules unifying with the given head atom.
		Set<NonGroundRule> ret = rulesUnifyingWithHead.get(groundHeadAtom);
		if (ret != null) {
			return ret;
		}
		// Construct rules unifying with the given head.
		return computeRulesHeadUnifyingWithGroundAtom(groundHeadAtom);
	}

	private Set<NonGroundRule> computeRulesHeadUnifyingWithGroundAtom(Atom groundAtom) {
		HashSet<NonGroundRule> nonGroundRules = getPredicateDefiningRules().get(groundAtom.getPredicate());
		if (nonGroundRules.isEmpty()) {
			return Collections.emptySet();
		}
		Set<NonGroundRule> ret = new LinkedHashSet<>();
		for (NonGroundRule nonGroundRule : nonGroundRules) {
			if (unify(nonGroundRule.getHeadAtom(), new Instance(groundAtom.getTerms()), new Substitution()) != null) {
				// Rule head does unify with current atom.
				ret.add(nonGroundRule);
			}
		}
		if (nonGroundRules.size() != 1) {
			// Only store the head-rules association if it likely will be used again.
			rulesUnifyingWithHead.put(groundAtom, ret);
		}
		return ret;
	}
}
