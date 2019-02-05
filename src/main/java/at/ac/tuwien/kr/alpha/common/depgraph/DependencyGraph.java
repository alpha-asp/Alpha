package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	private static final String CONSTRAINT_PREDICATE_FORMAT = "[constr_%d]";

	private static final Comparator<Node> NODE_COMP_DESC = (n1, n2) -> n2.getNodeInfo().getDfsFinishTime() - n1.getNodeInfo().getDfsFinishTime();

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the
	 * value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<Node, List<Edge>> nodes = new HashMap<>();

	/**
	 * The transposed graph structure, i.e. the same set of nodes, but all edges
	 * reversed - needed for analysis of strongly connected components. Note that
	 * this map must order it's keys by descending dfsFinishTime in order for the
	 * strongly connected component algorithm to work
	 */
	private TreeMap<Node, List<Edge>> transposedNodes;

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
	 * (Re)-Writes the <code>transposedNodes</code> of this
	 * <code>DependencyGraph</code>
	 */
	private void buildTransposedStructure() {
		TreeMap<Node, List<Edge>> transposed = new TreeMap<>(DependencyGraph.NODE_COMP_DESC);
		for (Map.Entry<Node, List<Edge>> entry : this.nodes.entrySet()) {
			for (Edge e : entry.getValue()) {
				if (!transposed.containsKey(e.getTarget())) {
					transposed.put(new Node(e.getTarget()), new ArrayList<>());
				}
				transposed.get(e.getTarget()).add(new Edge(entry.getKey(), e.getSign(), e.getLabel()));
			}
		}
		this.transposedNodes = transposed;
	}

	private Predicate generateConstraintDummyPredicate() {
		Predicate retVal = Predicate.getInstance(String.format(DependencyGraph.CONSTRAINT_PREDICATE_FORMAT, this.nextConstraintNumber()), 0);
		return retVal;
	}

	// TODO think up return type for SCCs
	private void findStronglyConnectedComponents() {
		DependencyGraphUtils.performDfs(this.nodes);
		this.buildTransposedStructure();
//		DependencyGraphUtils.performDfs(this.transposedNodes);
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

}
