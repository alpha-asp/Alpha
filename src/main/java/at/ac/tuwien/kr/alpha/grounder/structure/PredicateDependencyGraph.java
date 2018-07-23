package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Extracts and stores the dependency among predicates in a given program.
 * Copyright (c) 2017, the Alpha Team.
 */
public class PredicateDependencyGraph {

	private final Graph<Predicate> inputGraph;
	private final Map<Predicate, Set<Predicate>> dependencies;

	public Set<Predicate> getDependencies(Predicate predicate) {
		return Collections.unmodifiableSet(dependencies.get(predicate));
	}

	private PredicateDependencyGraph() {
		inputGraph = new Graph<>();
		dependencies = new HashMap<>();
	}

	private void addPredicate(Predicate predicate) {
		inputGraph.addVertex(predicate);
	}

	private void addEdge(Predicate from, Predicate to) {
		inputGraph.addEdge(from, to);
	}

	private void addEdges(Predicate from, List<Predicate> toPredicates) {
		for (Predicate toPredicate : toPredicates) {
			addEdge(from, toPredicate);
		}
	}

	public static PredicateDependencyGraph buildFromProgram(Program program) {
		PredicateDependencyGraph predicateDependencyGraph = new PredicateDependencyGraph();
		// Iterate over all rules and facts to initialize the graph.
		for (Rule rule : program.getRules()) {
			// Skip constraints.
			if (rule.isConstraint()) {
				continue;
			}

			Head ruleHead = rule.getHead();
			ArrayList<Predicate> predicatesInBody = new ArrayList<>(rule.getBody().size());
			for (Literal literal : rule.getBody()) {
				predicatesInBody.add(literal.getPredicate());
			}
			if (ruleHead instanceof ChoiceHead) {
				for (ChoiceHead.ChoiceElement choiceElement : ((ChoiceHead) ruleHead).getChoiceElements()) {
					Predicate fromPredicate = choiceElement.choiceAtom.getPredicate();
					for (Literal conditionLiteral : choiceElement.conditionLiterals) {
						predicateDependencyGraph.addEdge(fromPredicate, conditionLiteral.getPredicate());
					}
					predicateDependencyGraph.addEdges(fromPredicate, predicatesInBody);
				}
			} else if (ruleHead instanceof DisjunctiveHead) {
				for (Atom disjunctiveHeadAtom : ((DisjunctiveHead) ruleHead).disjunctiveAtoms) {
					Predicate fromPredicate = disjunctiveHeadAtom.getPredicate();
					predicateDependencyGraph.addEdges(fromPredicate, predicatesInBody);
				}
			} else {
				throw oops("Unknown rule head encountered.");
			}
		}
		// Add facts as vertices.
		for (Atom atom : program.getFacts()) {
			predicateDependencyGraph.addPredicate(atom.getPredicate());
		}

		// Compute transitive closure based on StronglyConnectedComponents.
		TarjanSCC<Predicate> tarjanSCC = new TarjanSCC<>(predicateDependencyGraph.inputGraph);
		predicateDependencyGraph.computeTransitiveClosure(tarjanSCC);

		return predicateDependencyGraph;
	}

	private void computeTransitiveClosure(TarjanSCC<Predicate> tarjanSCC) {
		// Note: We compute reachability on the graph of SCCs,
		//       since it is often smaller than the original graph and our reachability is O(n^3).

		// Get mapping SCC <-> position.
		Graph<TarjanSCC.SCC<Predicate>> sccGraph = tarjanSCC.getSccGraph();
		Map<TarjanSCC.SCC<Predicate>, Integer> sccToPosition = sccGraph.getVertexToPosition();
		List<TarjanSCC.SCC<Predicate>> positionToSCC = sccGraph.getPositionToVertex();

		// Initialize array for transitive closure.
		boolean reachable[][] = sccGraph.getAdjacencyMatrix().clone();

		// Connect all reachable nodes via node k.
		int size = sccGraph.getNumVertices();
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

		// Extract dependency among predicates from reachable matrix.
		for (Predicate predicate : inputGraph.getVertices()) {
			TarjanSCC.SCC<Predicate> predicateSCC = tarjanSCC.getVertexInSCC().get(predicate);
			// Predicate depends on all predicates in its SCC and those of SCCs its SCC depends on.
			Set<Predicate> dependsOn = new LinkedHashSet<>(predicateSCC.scc);
			for (int i = 0; i < size; i++) {
				Integer sccPosition = sccToPosition.get(predicateSCC);
				if (reachable[sccPosition][i]) {
					TarjanSCC.SCC<Predicate> reachableSCC = positionToSCC.get(i);
					dependsOn.addAll(reachableSCC.scc);
				}
			}
			dependencies.put(predicate, dependsOn);
		}
	}
}
