package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.depgraph.Node.NodeInfo;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	private static final String CONSTRAINT_PREDICATE_FORMAT = "[constr_%d]";

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<Node, List<Edge>> nodes = new HashMap<>();

	/**
	 * The transposed graph structure, i.e. the same set of nodes, but all edges reversed - needed for analysis of strongly connected components.
	 */
	private Map<Node, List<Edge>> transposedNodes;

	private Map<Integer, List<Node>> stronglyConnectedComponents;

	private int constraintNumber;

	public DependencyGraph() {
		// doing this since checkstyle doesn't let me initialize it directly to 0
		this.constraintNumber = 0;
	}

	public static DependencyGraph buildDependencyGraph(Map<Integer, NonGroundRule> nonGroundRules) {
		DependencyGraph retVal = new DependencyGraph();
		NonGroundRule tmpRule;
		Node tmpHeadNode;
		for (Map.Entry<Integer, NonGroundRule> entry : nonGroundRules.entrySet()) {
			tmpRule = entry.getValue();
			LOGGER.debug("Processing rule: {}", tmpRule);
			tmpHeadNode = retVal.handleRuleHead(tmpRule);
			for (Literal l : entry.getValue().getBodyLiterals()) {
				LOGGER.trace("Processing rule body literal: {}", l);
				if (l.getAtom().isBuiltin()) {
					LOGGER.trace("Ignoring builtin atom in literal {}", l);
					continue;
				}
				retVal.handleRuleBodyLiteral(tmpHeadNode, l);
			}
		}
		retVal.findStronglyConnectedComponents();
		return retVal;
	}

	private Node handleRuleHead(NonGroundRule rule) {
		LOGGER.trace("Processing head of rule: {}", rule);
		Node retVal;
		if (rule.isConstraint()) {
			Predicate p = this.generateConstraintDummyPredicate();
			retVal = new Node(p, p.toString(), true);
			List<Edge> dependencies = new ArrayList<>();
			dependencies.add(new Edge(retVal, false, "-"));
			if (this.nodes.containsKey(retVal)) {
				throw new IllegalStateException("Dependency graph already contains node for constraint " + p.toString() + "!");
			}
			this.nodes.put(retVal, dependencies);
		} else {
			Atom head = rule.getHeadAtom();
			retVal = new Node(head.getPredicate(), head.getPredicate().toString());
			if (!this.nodes.containsKey(retVal)) {
				LOGGER.trace("Creating new node for predicate {}", retVal.getPredicate());
				this.nodes.put(retVal, new ArrayList<>());
			}
		}
		return retVal;
	}

	private void handleRuleBodyLiteral(Node headNode, Literal lit) {
		List<Edge> dependants;
		Edge tmpEdge;
		Node bodyNode = new Node(lit.getPredicate(), lit.getPredicate().toString());
		if (!this.nodes.containsKey(bodyNode)) {
			LOGGER.trace("Creating new node for predicate {}", bodyNode.getPredicate());
			dependants = new ArrayList<>();
			this.nodes.put(bodyNode, dependants);
		} else {
			LOGGER.trace("Node for predicate {} already exists", bodyNode.getPredicate());
			dependants = this.nodes.get(bodyNode);
		}
		tmpEdge = new Edge(headNode, !lit.isNegated(), lit.isNegated() ? "-" : "+");
		if (!dependants.contains(tmpEdge)) {
			LOGGER.trace("Adding dependency: {} -> {} ({})", bodyNode.getPredicate(), headNode.getPredicate(), tmpEdge.getSign() ? "+" : "-");
			dependants.add(tmpEdge);
		}
	}

	/**
	 * (Re-)Writes the <code>transposedNodes</code> of this <code>DependencyGraph</code>
	 */
	private void buildTransposedStructure() {
		Map<Node, List<Edge>> transposed = new HashMap<>();
		Node tmpNodeCopy;
		Node srcNode;
		Node targetNode;
		for (Map.Entry<Node, List<Edge>> entry : this.nodes.entrySet()) {
			srcNode = entry.getKey();
			if (!transposed.containsKey(srcNode)) {
				// we don't wanna copy node info here, successive DFS runs shouldn't get messed up
				tmpNodeCopy = new Node(srcNode.getPredicate(), srcNode.getLabel(), srcNode.isConstraint(), new NodeInfo());
				transposed.put(tmpNodeCopy, new ArrayList<>());
			}
			for (Edge e : entry.getValue()) {
				targetNode = e.getTarget();
				if (!transposed.containsKey(e.getTarget())) {
					// we don't wanna copy node info here, successive DFS runs shouldn't get messed up
					tmpNodeCopy = new Node(targetNode.getPredicate(), targetNode.getLabel(), targetNode.isConstraint(), new NodeInfo());
					transposed.put(new Node(tmpNodeCopy), new ArrayList<>());
				}
				transposed.get(targetNode).add(new Edge(entry.getKey(), e.getSign(), e.getLabel()));
			}
		}
		this.transposedNodes = transposed;
	}

	private Predicate generateConstraintDummyPredicate() {
		Predicate retVal = Predicate.getInstance(String.format(DependencyGraph.CONSTRAINT_PREDICATE_FORMAT, this.nextConstraintNumber()), 0);
		return retVal;
	}

	// TODO maybe move this to DependencyGraphUtils
	// SCC algorithm as described in "Introduction to Algortihms" (Kosajaru-Algorithm)
	private void findStronglyConnectedComponents() {
		DfsResult intermediateResult = DependencyGraphUtils.performDfs(this.nodes.keySet().iterator(), this.nodes);
		this.buildTransposedStructure();
		Deque<Node> finishedNodes = intermediateResult.getFinishedNodes();
		DfsResult finalResult = DependencyGraphUtils.performDfs(finishedNodes.descendingIterator(), this.transposedNodes);
		int componentCnt = 0;
		Map<Integer, List<Node>> componentMap = new HashMap<>();
		List<Node> tmpComponentMembers;
		for (Node componentRoot : finalResult.getDepthFirstForest().get(null)) {
			tmpComponentMembers = new ArrayList<>();
			this.addComponentMembers(componentRoot, finalResult.getDepthFirstForest(), tmpComponentMembers);
			componentMap.put(componentCnt, tmpComponentMembers);
			componentCnt++;
		}
		this.stronglyConnectedComponents = componentMap;
	}

	private void addComponentMembers(Node depthFirstTreeNode, Map<Node, List<Node>> depthFirstForest, List<Node> componentMembers) {
		componentMembers.add(depthFirstTreeNode);
		List<Node> children;
		if ((children = depthFirstForest.get(depthFirstTreeNode)) == null) {
			return;
		}
		for (Node n : children) {
			this.addComponentMembers(n, depthFirstForest, componentMembers);
		}
	}

	private int nextConstraintNumber() {
		return ++this.constraintNumber;
	}

	public Map<Node, List<Edge>> getNodes() {
		return this.nodes;
	}

	public Map<Node, List<Edge>> getTransposedNodes() {
		return this.transposedNodes;
	}

	public Map<Integer, List<Node>> getStronglyConnectedComponents() {
		return this.stronglyConnectedComponents;
	}

}
