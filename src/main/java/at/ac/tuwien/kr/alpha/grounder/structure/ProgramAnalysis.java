package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ProgramAnalysis {

	private final Map<Predicate, HashSet<NonGroundRule>> predicateDefiningRules;
	private final PredicateDependencyGraph predicateDependencyGraph;

	public ProgramAnalysis(Program program) {
		predicateDefiningRules = new HashMap<>();
		predicateDependencyGraph = PredicateDependencyGraph.buildFromProgram(program);
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}
}
