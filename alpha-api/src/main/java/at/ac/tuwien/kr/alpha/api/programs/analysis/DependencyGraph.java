package at.ac.tuwien.kr.alpha.api.programs.analysis;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;

import java.util.List;
import java.util.Map;

/**
 * The predicate-level dependency graph of an ASP program.
 * Each node of the dependency graph represents one {@link Predicate}.
 * An edge between from node A to node B indicates that predicate B depends on predicate A. Edges also store a "sign" (true or false)
 * indicating whether a dependency is positive or negative (i.e. the predicate A occurs negated in the rule body deriving B).
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface DependencyGraph {

	Node getNodeForPredicate(Predicate p);

	Map<Node, List<Edge>> getAdjancencyMap();

	interface Edge {

		Node getTarget();

		boolean getSign();

	}

	interface Node {

		Predicate getPredicate();

		String getLabel();

	}
}
