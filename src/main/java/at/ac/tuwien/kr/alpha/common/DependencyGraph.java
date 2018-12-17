package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	private static final String FACT_NODE_FORMAT = "F%d";
	private static final String RULE_NODE_FORMAT = "R%d";

	// TODO type for node, include a label - not sure, a node is actually a string
	public class Edge {
		public final String target;
		public final boolean sign;

		/**
		 * Creates a new edge of a dependency graph. Read as "target depends on source"
		 * Sign indicates if the dependency is positive or negative (target node depends
		 * on default negated atom). NOTE: Working assumption is to treat strong
		 * negation as a positive dependency
		 * 
		 * @param target
		 * @param sign
		 */
		public Edge(String target, boolean sign) {
			this.target = target;
			this.sign = sign;
		}

		@Override
		public boolean equals(Object o) {
			return ("" + this.target + Boolean.toString(this.sign)).equals(o);
		}

		@Override
		public int hashCode() {
			return ("" + this.target + Boolean.toString(this.sign)).hashCode();
		}

	}

	private Map<Predicate, Map<Instance, String>> factsToNodes = new HashMap<>();
	private Map<Predicate, List<String>> ruleHeadsToNodes = new HashMap<>();

	private int nodeIdCounter = -1;

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the
	 * value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<String, List<Edge>> nodes = new HashMap<>();

	/**
	 * Adds a new node to this DependencyGraph. Must only be called with node IDs
	 * not yet present in the graph.
	 * 
	 * @param nodeId id of the new node
	 * @param edges  zero or more edges to add to the new node
	 * 
	 * @throws IllegalArgumentException in case a node with the given id already
	 *                                  exists in the graph
	 */
	private void addNode(String nodeId, List<Edge> edges) {
		if (this.nodes.containsKey(nodeId)) {
			throw new IllegalArgumentException("Node map already contains node id " + nodeId + "as a key!");
		}
		this.nodes.put(nodeId, edges);
	}

	private void addEdge(String nodeIdFrom, Edge e) {
		if (!this.nodes.containsKey(nodeIdFrom)) {
			throw new IllegalArgumentException("Node with id " + nodeIdFrom + " doesn't exist!");
		}
		this.nodes.get(nodeIdFrom).add(e);
	}

	private void addDependencies(String dependentNodeId, Predicate bodyPredicate, boolean isNegative) {
		if (this.ruleHeadsToNodes.containsKey(bodyPredicate)) {
			for (String dependencyNodeId : this.ruleHeadsToNodes.get(bodyPredicate)) {
				this.addEdge(dependencyNodeId, new Edge(dependentNodeId, !isNegative));
			}
		} else {
			if (this.factsToNodes.containsKey(bodyPredicate)) {
				for (Map.Entry<Instance, String> entry : this.factsToNodes.get(bodyPredicate).entrySet()) {
					this.addEdge(entry.getValue(), new Edge(dependentNodeId, !isNegative));
				}
			} else {
				// create new node for body predicate, i.e. assume there will be a rule head.
				// if there's a non-ground node without incoming edges we know it can't become
				// true
				String dependencyNodeId = String.format(DependencyGraph.RULE_NODE_FORMAT, this.nextNodeId());
				List<Edge> edges = new ArrayList<>();
				edges.add(new Edge(dependentNodeId, !isNegative));
				this.addNode(dependencyNodeId, edges);
			}
		}
	}
	
	public static DependencyGraph buildDependencyGraph(Map<Predicate, LinkedHashSet<Instance>> factsFromProgram,
			Map<Integer, NonGroundRule> nonGroundRules) {
		DependencyGraph retVal = new DependencyGraph();
		Map<Instance, String> tmp = null;
		int i = 0;
		String tmpNodeId = null;
		for (Map.Entry<Predicate, LinkedHashSet<Instance>> entry : factsFromProgram.entrySet()) {
			for (Instance inst : entry.getValue()) {
				LOGGER.debug("Predicate instance from facts: {} {}", entry.getKey().toString(), inst.toString());
				tmpNodeId = String.format(DependencyGraph.FACT_NODE_FORMAT, retVal.nextNodeId());
				retVal.addNode(tmpNodeId, new ArrayList<>());
				tmp = new HashMap<>();
				tmp.put(inst, tmpNodeId);
				retVal.factsToNodes.put(entry.getKey(), tmp);
				i++;
			}
		}
		NonGroundRule tmpRule = null;
		Predicate tmpBodyPredicate = null;
		for (Map.Entry<Integer, NonGroundRule> ruleEntry : nonGroundRules.entrySet()) {
			LOGGER.debug("NonGroundRule id = {}: {}", ruleEntry.getKey().toString(), ruleEntry.getValue().toString());
			// TODO process rule head, check if dependants exist and add those edges
			tmpNodeId = String.format(DependencyGraph.RULE_NODE_FORMAT, retVal.nextNodeId());
			retVal.addNode(tmpNodeId, new ArrayList<>());
			tmpRule = ruleEntry.getValue();
			for (Literal lit : tmpRule.getBodyLiterals()) {
				tmpBodyPredicate = lit.getPredicate();
				retVal.addDependencies(tmpNodeId, tmpBodyPredicate, lit.isNegated());
			}
		}
		return null;
	}

	private int nextNodeId() {
		return ++this.nodeIdCounter;
	}

	public Map<Predicate, Map<Instance, String>> getFactsToNodes() {
		return this.factsToNodes;
	}

	public Map<Predicate, List<String>> getRuleHeadsToNodes() {
		return this.ruleHeadsToNodes;
	}

	public Map<String, List<Edge>> getNodes() {
		return this.nodes;
	}

}
