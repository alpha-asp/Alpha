package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

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
			if (!(o instanceof Edge)) {
				return false;
			}
			Edge other = (Edge) o;
			return this.target.equals(other.target) && this.sign == other.sign;
		}

		@Override
		public int hashCode() {
			return ("" + this.target.predicate.toString() + Boolean.toString(this.sign)).hashCode();
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

		private final Predicate predicate;
		private final Map<Integer, NonGroundRule> producingRules = new HashMap<>();
		private final String label;

		public Node(Predicate predicate, String label) {
			this.predicate = predicate;
			this.label = label;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Node)) {
				return false;
			}
			return this.predicate.equals(((Node) o).predicate);
		}

		@Override
		public int hashCode() {
			return this.predicate.hashCode();
		}

		public String getLabel() {
			return this.label;
		}

		public Predicate getPredicate() {
			return this.predicate;
		}

		public Map<Integer, NonGroundRule> getProducingRules() {
			return this.producingRules;
		}

	}

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the
	 * value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<Node, List<Edge>> nodes = new HashMap<>();

	// TODO we need to handle builtin atoms since those don't depend on rules within
	// the program
	public static DependencyGraph buildDependencyGraph(Map<Integer, NonGroundRule> nonGroundRules) {
		DependencyGraph retVal = new DependencyGraph();
		NonGroundRule tmpRule;
		Atom tmpHead;
		Node tmpHeadNode;
		Node tmpBodyNode;
		List<Edge> tmpDependants;
		Edge tmpEdge;
		for (Map.Entry<Integer, NonGroundRule> entry : nonGroundRules.entrySet()) {
			tmpRule = entry.getValue();
			LOGGER.trace("Processing rule: {}", tmpRule);
			tmpHead = tmpRule.getHeadAtom();
			tmpHeadNode = retVal.new Node(tmpHead.getPredicate(), tmpHead.getPredicate().toString());
			if (!retVal.nodes.containsKey(tmpHeadNode)) {
				LOGGER.trace("Creating new node for predicate {}", tmpHeadNode.predicate);
				retVal.nodes.put(tmpHeadNode, new ArrayList<>());
			}
			for (Literal l : entry.getValue().getBodyLiterals()) {
				LOGGER.trace("Processing rule body literal: {}", l);
				tmpBodyNode = retVal.new Node(l.getPredicate(), l.getPredicate().toString());
				if (!retVal.nodes.containsKey(tmpBodyNode)) {
					LOGGER.trace("Creating new node for predicate {}", tmpBodyNode.predicate);
					tmpDependants = new ArrayList<>();
					retVal.nodes.put(tmpBodyNode, tmpDependants);
				} else {
					LOGGER.trace("Node for predicate {} already exists", tmpBodyNode.predicate);
					tmpDependants = retVal.nodes.get(tmpBodyNode);
				}
				tmpEdge = retVal.new Edge(tmpHeadNode, !l.isNegated(), l.isNegated() ? "-" : "+");
				if (!tmpDependants.contains(tmpEdge)) {
					LOGGER.trace("Adding dependency: {} -> {} ({})", tmpBodyNode.predicate, tmpHeadNode.predicate,
							tmpEdge.sign ? "+" : "-");
					tmpDependants.add(tmpEdge);
				}
			}
		}
		return retVal;
	}

	public Map<Node, List<Edge>> getNodes() {
		return this.nodes;
	}

}
