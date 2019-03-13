package at.ac.tuwien.kr.alpha.grounder.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.depgraph.DependencyGraph;
import at.ac.tuwien.kr.alpha.common.rule.impl.Rule;
import at.ac.tuwien.kr.alpha.grounder.FactIntervalEvaluator;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

/**
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public class ProgramAnalysis {

	private final Map<Predicate, HashSet<NonGroundRule>> predicateDefiningRules = new HashMap<>();
	private final Map<Predicate, LinkedHashSet<Instance>> programFacts = new LinkedHashMap<>();
	private final Map<Integer, NonGroundRule> nonGroundRules = new HashMap<>();
	private final DependencyGraph dependencyGraph;

	public ProgramAnalysis(Program program) {
		this.initializeFacts(program);
		this.initializeRules(program);
		this.dependencyGraph = DependencyGraph.buildDependencyGraph(this.nonGroundRules);
	}

	private void initializeFacts(Program program) {
		List<Instance> tmpInstances;
		Predicate tmpPredicate;
		for (Atom fact : program.getFacts()) {
			tmpInstances = FactIntervalEvaluator.constructFactInstances(fact);
			tmpPredicate = fact.getPredicate();
			this.programFacts.putIfAbsent(tmpPredicate, new LinkedHashSet<>());
			this.programFacts.get(tmpPredicate).addAll(tmpInstances);
		}
	}

	private Map<Integer, NonGroundRule> initializeRules(Program program) {
		Map<Integer, NonGroundRule> retVal = new HashMap<>();
		NonGroundRule tmpRule;
		for (Rule rule : program.getRules()) {
			tmpRule = NonGroundRule.constructNonGroundRule(rule);
			this.nonGroundRules.put(tmpRule.getRuleId(), tmpRule);
			if (tmpRule.getHeadAtom() != null) {
				// rule is not a constraint, register the predicate it defines
				this.recordDefiningRule(tmpRule.getHeadAtom().getPredicate(), tmpRule);
			}
		}
		return retVal;
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		this.predicateDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
		this.predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(this.predicateDefiningRules);
	}

	public Map<Predicate, LinkedHashSet<Instance>> getProgramFacts() {
		return this.programFacts;
	}

	public Map<Integer, NonGroundRule> getNonGroundRules() {
		return this.nonGroundRules;
	}

	public DependencyGraph getDependencyGraph() {
		return this.dependencyGraph;
	}
}
