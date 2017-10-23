package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import com.google.common.collect.HashBiMap;

import java.util.*;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class PredicateDependencyGraph {

	private final Set<Predicate> predicates;
	private final Map<Predicate, Predicate> edges;
	private final Map<Predicate, Set<Predicate>> dependencies;

	public Set<Predicate> getDependencies(Predicate predicate) {
		return Collections.unmodifiableSet(dependencies.get(predicate));
	}

	private PredicateDependencyGraph() {
		predicates = new LinkedHashSet<>();
		edges = new LinkedHashMap<>();
		dependencies = new HashMap<>();
	}

	private void addPredicate(Predicate predicate) {
		predicates.add(predicate);
	}

	private void addEdge(Predicate from, Predicate to) {
		addPredicate(from);
		addPredicate(to);
		edges.put(from, to);
	}

	public static PredicateDependencyGraph buildFromProgram(Program program) {
		PredicateDependencyGraph predicateDependencyGraph = new PredicateDependencyGraph();
		// Iterate over all rules and facts to initialize the graph.
		for (Rule rule : program.getRules()) {
			Atom head = rule.getHead();
			// Skip constraints.
			if (head == null) {
				continue;
			}
			List<Predicate> fromPredicates = Collections.singletonList(head.getPredicate());
			/*if (head instanceof ChoiceAtom) {
				// TODO: treat choice heads and disjunction here, i.e., have more fromPredicates.
			}*/
			LinkedHashSet<Predicate> toPredicates = new LinkedHashSet<>();
			for (Literal literal : rule.getBody()) {
				toPredicates.add(literal.getPredicate());
				// TODO: aggregates in body require more treatment here.
			}
			// Create and add edges.
			for (Predicate fromPredicate : fromPredicates) {
				for (Predicate toPredicate : toPredicates) {
					predicateDependencyGraph.addEdge(fromPredicate, toPredicate);
				}
			}
		}
		// Add facts as vertices.
		for (Atom atom : program.getFacts()) {
			predicateDependencyGraph.addPredicate(atom.getPredicate());
		}

		// Compute transitive closure.
		predicateDependencyGraph.computeTransitiveClosure();
		return predicateDependencyGraph;
	}

	private void computeTransitiveClosure() {
		// Associate each predicate a position.
		int size = predicates.size();
		HashBiMap<Predicate, Integer> predicatePosition = HashBiMap.create(size);
		int pos = 0;
		for (Predicate predicate : predicates) {
			predicatePosition.put(predicate, pos++);
		}
		// Initialize array for transitive closure.
		boolean reachable[][] = new boolean[size][size];
		for (Map.Entry<Predicate, Predicate> edge : edges.entrySet()) {
			reachable[predicatePosition.get(edge.getKey())][predicatePosition.get(edge.getValue())] = true;
		}
		// Connect all reachable nodes via node k.
		for (int k = 0; k < size; k++) {
			// Check all pair of nodes nodes i,j whether they are connected via k.
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if (reachable[i][k] && reachable[k][j]) {
						// Record connection.
						reachable[i][j] = true;
					}
				}
			}
		}
		// Store reachable predicates as dependencies.
		for (Predicate predicate : predicates) {
			Set<Predicate> dependsOn = new LinkedHashSet<>();
			for (int i = 0; i < size; i++) {
				if (reachable[predicatePosition.get(predicate)][i]) {
					dependsOn.add(predicatePosition.inverse().get(i));
				}
			}
			dependencies.put(predicate, dependsOn);
		}
	}
}
