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

	private final Map<Predicate, HashSet<NonGroundRule>> predicateDefiningRules;
	private final Map<BasicAtom, LinkedHashSet<RuleAtom>> completionBodies;

	public ProgramAnalysis(Program program) {
		predicateDefiningRules = new LinkedHashMap<>();
		completionBodies = new LinkedHashMap<>();
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	public Collection<RuleAtom> getCompletionBodies(BasicAtom ruleHeadWithNormalizedVariables) {
		// TODO: we could compute completions not for predicates but for the same partially-ground head atoms.
		// TODO: Example: p(a,Y) :- q(Y). and p(b,Y) :- r(X,Y,Z). is fine for p(a,_).
		// TODO: But: p(a,Y) :- q(Y). and p(X,Y) :- r(X,Y,Z). is not fine for p(a,_).
		// TODO: in order to work on partially-instantiated atoms, we have to check whether rule heads unify (or one is more general than the other).
		return Collections.unmodifiableSet(completionBodies.get(ruleHeadWithNormalizedVariables));
	}

	public void computeCompletionBodies() {
		for (Map.Entry<Predicate, HashSet<NonGroundRule>> predicateDefiningRules : predicateDefiningRules.entrySet()) {
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
