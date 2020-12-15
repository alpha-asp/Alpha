package at.ac.tuwien.kr.alpha.common.program;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.grounder.FactIntervalEvaluator;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A program in the internal representation needed for grounder and solver, i.e.: rules must have normal heads, all
 * aggregates must be rewritten, all intervals must be preprocessed (into interval atoms), and equality predicates must
 * be rewritten.
 * 
 * Copyright (c) 2017-2020, the Alpha Team.
 */
public class InternalProgram extends AbstractProgram<InternalRule> {

	private final Map<Predicate, LinkedHashSet<InternalRule>> predicateDefiningRules = new LinkedHashMap<>();
	private final Map<Predicate, LinkedHashSet<Instance>> factsByPredicate = new LinkedHashMap<>();
	private final Map<Integer, InternalRule> rulesById = new LinkedHashMap<>();
	private TreeMap<Atom, Set<InternalRule>> rulesUnifyingWithHead = new TreeMap<>();
	private Set<InternalRule> isFullyNonProjective = new HashSet<>();

	public InternalProgram(List<InternalRule> rules, List<Atom> facts) {
		super(rules, facts, null);
		recordFacts(facts);
		recordRules(rules);
	}

	static ImmutablePair<List<InternalRule>, List<Atom>> internalizeRulesAndFacts(NormalProgram normalProgram) {
		List<InternalRule> internalRules = new ArrayList<>();
		List<Atom> facts = new ArrayList<>(normalProgram.getFacts());
		for (NormalRule r : normalProgram.getRules()) {
			if (r.getBody().isEmpty()) {
				if (!r.getHead().isGround()) {
					throw new IllegalArgumentException("InternalProgram does not support non-ground rules with empty bodies! (Head = " + r.getHead().toString() + ")");
				}
				facts.add(r.getHeadAtom());
			} else {
				internalRules.add(InternalRule.fromNormalRule(r));
			}
		}
		return new ImmutablePair<>(internalRules, facts);
	}

	public static InternalProgram fromNormalProgram(NormalProgram normalProgram) {
		ImmutablePair<List<InternalRule>, List<Atom>> rulesAndFacts = InternalProgram.internalizeRulesAndFacts(normalProgram);
		return new InternalProgram(rulesAndFacts.left, rulesAndFacts.right);
	}

	private void recordFacts(List<Atom> facts) {
		for (Atom fact : facts) {
			List<Instance> tmpInstances = FactIntervalEvaluator.constructFactInstances(fact);
			Predicate tmpPredicate = fact.getPredicate();
			factsByPredicate.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
			factsByPredicate.get(tmpPredicate).addAll(tmpInstances);
		}
	}

	private void recordRules(List<InternalRule> rules) {
		for (InternalRule rule : rules) {
			rulesById.put(rule.getRuleId(), rule);
			if (!rule.isConstraint()) {
				recordDefiningRule(rule.getHeadAtom().getPredicate(), rule);
			}
		}
	}

	private void recordDefiningRule(Predicate headPredicate, InternalRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new LinkedHashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, LinkedHashSet<InternalRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	public Map<Predicate, LinkedHashSet<Instance>> getFactsByPredicate() {
		return Collections.unmodifiableMap(factsByPredicate);
	}

	public Map<Integer, InternalRule> getRulesById() {
		return Collections.unmodifiableMap(rulesById);
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
		for (Map.Entry<Predicate, LinkedHashSet<InternalRule>> definingRules : predicateDefiningRules.entrySet()) {
			isCompletable = true;
			LinkedHashSet<InternalRule> rules = definingRules.getValue();
			for (InternalRule rule : rules) {
				if (!rule.isNonProjective() && !rule.isFunctionallyDependent()) {
					isCompletable = false;
				}
			}
			if (isCompletable) {
				this.isFullyNonProjective.addAll(rules);
			}
		}
	}

	public boolean isRuleFullyNonProjective(InternalRule nonGroundRule) {
		return isFullyNonProjective.contains(nonGroundRule);
	}

	public Set<InternalRule> getRulesUnifyingWithGroundHead(Atom groundHeadAtom) {
		// Check if we already computed all rules unifying with the given head atom.
		Set<InternalRule> ret = rulesUnifyingWithHead.get(groundHeadAtom);
		if (ret != null) {
			return ret;
		}
		// Construct rules unifying with the given head.
		return computeRulesHeadUnifyingWithGroundAtom(groundHeadAtom);
	}

	private Set<InternalRule> computeRulesHeadUnifyingWithGroundAtom(Atom groundAtom) {
		HashSet<InternalRule> nonGroundRules = getPredicateDefiningRules().get(groundAtom.getPredicate());
		if (nonGroundRules.isEmpty()) {
			return Collections.emptySet();
		}
		Set<InternalRule> ret = new LinkedHashSet<>();
		for (InternalRule nonGroundRule : nonGroundRules) {
			if (Substitution.specializeSubstitution(nonGroundRule.getHeadAtom(), new Instance(groundAtom.getTerms()), new Substitution()) != null) {
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
