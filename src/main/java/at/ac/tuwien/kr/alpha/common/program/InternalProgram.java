package at.ac.tuwien.kr.alpha.common.program;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.grounder.FactIntervalEvaluator;
import at.ac.tuwien.kr.alpha.grounder.Instance;

/**
 * A program in the internal representation needed for grounder and solver, i.e.: rules must have normal heads, all
 * aggregates must be rewritten, all intervals
 * must be preprocessed (into interval atoms), equality predicates must be rewritten
 * 
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public class InternalProgram extends AbstractProgram<InternalRule> {

	private final Map<Predicate, HashSet<InternalRule>> predicateDefiningRules = new LinkedHashMap<>();
	private final Map<Predicate, LinkedHashSet<Instance>> factsByPredicate = new LinkedHashMap<>();
	private final Map<Integer, InternalRule> rulesById = new LinkedHashMap<>();

	public InternalProgram(List<InternalRule> rules, List<Atom> facts) {
		super(rules, facts, null);
		this.recordFacts(facts);
		this.recordRules(rules);
	}

	protected static ImmutablePair<List<InternalRule>, List<Atom>> internalizeRulesAndFacts(NormalProgram normalProgram) {
		List<InternalRule> internalRules = new ArrayList<>();
		List<Atom> facts = new ArrayList<>(normalProgram.getFacts());
		for (NormalRule r : normalProgram.getRules()) {
			if (r.getBody().isEmpty()) {
				if (!r.getHead().isGround()) {
					throw new IllegalArgumentException(
							"InternalProgram does not support non-ground rules with empty bodies! (Head = " + r.getHead().toString() + ")");
				} else {
					facts.add(r.getHeadAtom());
				}
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
		List<Instance> tmpInstances;
		Predicate tmpPredicate;
		for (Atom fact : facts) {
			tmpInstances = FactIntervalEvaluator.constructFactInstances(fact);
			tmpPredicate = fact.getPredicate();
			this.factsByPredicate.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
			this.factsByPredicate.get(tmpPredicate).addAll(tmpInstances);
		}
	}

	private Map<Integer, InternalRule> recordRules(List<InternalRule> rules) {
		Map<Integer, InternalRule> retVal = new HashMap<>();
		for (InternalRule rule : rules) {
			this.rulesById.put(rule.getRuleId(), rule);
			if (!rule.isConstraint()) {
				this.recordDefiningRule(rule.getHeadAtom().getPredicate(), rule);
			}
		}
		return retVal;
	}

	private void recordDefiningRule(Predicate headPredicate, InternalRule rule) {
		this.predicateDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
		this.predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<InternalRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(this.predicateDefiningRules);
	}

	public Map<Predicate, LinkedHashSet<Instance>> getFactsByPredicate() {
		return Collections.unmodifiableMap(this.factsByPredicate);
	}

	public Map<Integer, InternalRule> getRulesById() {
		return Collections.unmodifiableMap(this.rulesById);
	}

}
