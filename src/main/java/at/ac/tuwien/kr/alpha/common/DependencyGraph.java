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

	public class Edge {

		private final Node target;
		private final boolean sign;
		private final String label;

		/**
		 * Creates a new edge of a dependency graph. Read as "target depends on source"
		 * Sign indicates if the dependency is positive or negative (target node depends
		 * on default negated atom). NOTE: Working assumption is to treat strong
		 * negation as a positive dependency
		 * 
		 * @param target
		 * @param sign
		 */
		public Edge(Node target, boolean sign, String label) {
			this.target = target;
			this.sign = sign;
			this.label = label;
		}

		@Override
		public boolean equals(Object o) {
			return ("" + this.target + Boolean.toString(this.sign)).equals(o);
		}

		@Override
		public int hashCode() {
			return ("" + this.target + Boolean.toString(this.sign)).hashCode();
		}

		public Node getTarget() {
			return this.target;
		}

		public boolean getSign() {
			return this.sign;
		}

		public String getLabel() {
			return this.label;
		}

	}

	public class Node {

		private final String id;
		private final String label;

		public Node(String id, String label) {
			this.id = id;
			this.label = label;
		}

		@Override
		public boolean equals(Object o) {
			return this.id.equals(o);
		}

		@Override
		public int hashCode() {
			return this.id.hashCode();
		}

		public String getId() {
			return this.id;
		}

		public String getLabel() {
			return this.label;
		}

	}

	private Map<Predicate, Map<Instance, Node>> factsToNodes = new HashMap<>();
	private Map<Predicate, List<Node>> ruleHeadsToNodes = new HashMap<>();

	private int nodeIdCounter = -1;

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the
	 * value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<Node, List<Edge>> nodes = new HashMap<>();

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
	private Node addNode(String nodeId, String nodeLabel, List<Edge> edges) {
		Node n = new Node(nodeId, nodeLabel);
		if (this.nodes.containsKey(n)) {
			throw new IllegalArgumentException("Node map already contains node id " + nodeId + "as a key!");
		}
		this.nodes.put(n, edges);
		return n;
	}

	private void addEdge(Node nodeFrom, Edge e) {
		if (!this.nodes.containsKey(nodeFrom)) {
			throw new IllegalArgumentException("Node with id " + nodeFrom.id + " doesn't exist!");
		}
		this.nodes.get(nodeFrom).add(e);
	}

	// TODO rather than just a predicate, pass the "real thing" here
	private void addDependencies(Node dependentNode, Predicate bodyPredicate, boolean isNegative) {
		if (this.ruleHeadsToNodes.containsKey(bodyPredicate)) {
			for (Node dependencyNode : this.ruleHeadsToNodes.get(bodyPredicate)) {
				this.addEdge(dependencyNode,
						new Edge(dependentNode, !isNegative, isNegative ? "-" : "+" + " " + bodyPredicate.toString()));
			}
		} else {
			if (this.factsToNodes.containsKey(bodyPredicate)) {
				for (Map.Entry<Instance, Node> entry : this.factsToNodes.get(bodyPredicate).entrySet()) {
					this.addEdge(entry.getValue(), new Edge(dependentNode, !isNegative,
							isNegative ? "-" : "+" + " " + bodyPredicate.toString()));
				}
			} else {
				// create new node for body predicate, i.e. assume there will be a rule head.
				// if there's a non-ground node without incoming edges we know it can't become
				// true
				String dependencyNodeId = String.format(DependencyGraph.RULE_NODE_FORMAT, this.nextNodeId());
				List<Edge> edges = new ArrayList<>();
				edges.add(
						new Edge(dependentNode, !isNegative, isNegative ? "-" : "+" + " " + bodyPredicate.toString()));
				this.addNode(dependencyNodeId, bodyPredicate.toString(), edges);
			}
		}
	}

	// TODO we need to handle builtin atoms since those don't depend on rules within
	// the program
	public static DependencyGraph buildDependencyGraph(Map<Predicate, LinkedHashSet<Instance>> factsFromProgram,
			Map<Integer, NonGroundRule> nonGroundRules) {
		DependencyGraph retVal = new DependencyGraph();
		Map<Instance, Node> tmp = null;
		String tmpNodeId = null;
		String tmpNodeLabel = null;
		Node tmpNode;
		// TODO maybe we should'nt make one graph node of every fact instance
		// (hierarchical??)
		for (Map.Entry<Predicate, LinkedHashSet<Instance>> entry : factsFromProgram.entrySet()) {
			tmp = new HashMap<>();
			for (Instance inst : entry.getValue()) {
				LOGGER.debug("Predicate instance from facts: {} {}", entry.getKey().toString(), inst.toString());
				tmpNodeId = String.format(DependencyGraph.FACT_NODE_FORMAT, retVal.nextNodeId());
				tmpNodeLabel = entry.getKey().getName() + inst.toString();
				tmpNode = retVal.addNode(tmpNodeId, tmpNodeLabel, new ArrayList<>());
				tmp.put(inst, tmpNode);
			}
			retVal.factsToNodes.put(entry.getKey(), tmp);
		}
		NonGroundRule tmpRule = null;
		Predicate tmpBodyPredicate = null;
		List<Node> tmpRuleHeadNodes = null;
		for (Map.Entry<Integer, NonGroundRule> ruleEntry : nonGroundRules.entrySet()) {
			LOGGER.debug("NonGroundRule id = {}: {}", ruleEntry.getKey().toString(), ruleEntry.getValue().toString());
			// TODO process rule head, check if dependants exist and add those edges
			tmpRule = ruleEntry.getValue();
			tmpNodeId = String.format(DependencyGraph.RULE_NODE_FORMAT, retVal.nextNodeId());
			tmpNodeLabel = tmpRule.getHeadAtom().toString();
			tmpNode = retVal.addNode(tmpNodeId, tmpNodeLabel, new ArrayList<>());
			if (!retVal.ruleHeadsToNodes.containsKey(tmpRule.getHeadAtom().getPredicate())) {
				tmpRuleHeadNodes = new ArrayList<>();
				tmpRuleHeadNodes.add(tmpNode);
				retVal.ruleHeadsToNodes.put(tmpRule.getHeadAtom().getPredicate(), tmpRuleHeadNodes);
			} else {
				retVal.ruleHeadsToNodes.get(tmpRule.getHeadAtom().getPredicate()).add(tmpNode);
			}
			for (Literal lit : tmpRule.getBodyLiterals()) {
				tmpBodyPredicate = lit.getPredicate();
				if (!tmpBodyPredicate.isInternal()) {
					retVal.addDependencies(tmpNode, tmpBodyPredicate, lit.isNegated());
				}
			}
		}
		return retVal;
	}

	private int nextNodeId() {
		return ++this.nodeIdCounter;
	}

	public Map<Predicate, Map<Instance, Node>> getFactsToNodes() {
		return this.factsToNodes;
	}

	public Map<Predicate, List<Node>> getRuleHeadsToNodes() {
		return this.ruleHeadsToNodes;
	}

	public Map<Node, List<Edge>> getNodes() {
		return this.nodes;
	}

}
