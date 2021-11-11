package at.ac.tuwien.kr.alpha.api.programs.analysis;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The component graph of an ASP program. The nodes of a component graph are strongly-connected components of the underlying
 * {@link DependencyGraph}. Component graphs are always directed acyclic graphs. Every component (i.e. graph node) represents a set of
 * predicates that cyclically depend on each other. Predicates that are not part of dependency cycles form components of their own. Paths go
 * from components to those components that depend on them.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ComponentGraph {

	/**
	 * The full list of nodes (i.e. sets of predicates) in this graph.
	 */
	List<SCComponent> getComponents();

	/**
	 * The list of components that have no incoming edges (i.e. predicates that do not depend on other predicates) in the graph.
	 */
	List<SCComponent> getEntryPoints();

	interface SCComponent {

		Map<Integer, Boolean> getDependencyIds();

		Set<Integer> getDependentIds();

		boolean hasNegativeCycle();

		List<DependencyGraph.Node> getNodes();

		int getId();

	}

}
