package at.ac.tuwien.kr.alpha.common.program;

import at.ac.tuwien.kr.alpha.common.PredicateImpl;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.AtomImpl;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.grounder.FactIntervalEvaluator;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

/**
 * A program in the internal representation needed for grounder and solver, i.e.: rules must have normal heads, all
 * aggregates must be rewritten, all intervals must be preprocessed (into interval atoms), and equality predicates must
 * be rewritten.
 * <p>
 * Copyright (c) 2017-2020, the Alpha Team.
 */
public class InternalProgram extends AbstractProgram<InternalRule> {

	private final Map<PredicateImpl, LinkedHashSet<InternalRule>> predicateDefiningRules = new LinkedHashMap<>();
	private final Map<PredicateImpl, LinkedHashSet<Instance>> factsByPredicate = new LinkedHashMap<>();
	private final Map<Integer, InternalRule> rulesById = new LinkedHashMap<>();

	public InternalProgram(List<InternalRule> rules, List<? extends AtomImpl> facts) {
		super(rules, facts, null);
		recordFacts(facts);
		recordRules(rules);
	}

	static ImmutablePair<List<InternalRule>, List<? extends AtomImpl>> internalizeRulesAndFacts(NormalProgram normalProgram) {
		List<InternalRule> internalRules = new ArrayList<>();
		List<AtomImpl> facts = new ArrayList<>(normalProgram.getFacts());
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
		ImmutablePair<List<InternalRule>, List<? extends AtomImpl>> rulesAndFacts = InternalProgram.internalizeRulesAndFacts(normalProgram);
		return new InternalProgram(rulesAndFacts.left, rulesAndFacts.right);
	}

	private void recordFacts(List<? extends AtomImpl> facts) {
		for (AtomImpl fact : facts) {
			List<Instance> tmpInstances = FactIntervalEvaluator.constructFactInstances(fact);
			PredicateImpl tmpPredicate = fact.getPredicate();
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

	private void recordDefiningRule(PredicateImpl headPredicate, InternalRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new LinkedHashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<PredicateImpl, LinkedHashSet<InternalRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	public Map<PredicateImpl, LinkedHashSet<Instance>> getFactsByPredicate() {
		return Collections.unmodifiableMap(factsByPredicate);
	}

	public Map<Integer, InternalRule> getRulesById() {
		return Collections.unmodifiableMap(rulesById);
	}

}
