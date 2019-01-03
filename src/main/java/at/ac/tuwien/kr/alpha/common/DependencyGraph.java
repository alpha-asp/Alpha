package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	private static final String CONSTRAINT_PREDICATE_FORMAT = "[constr_%d]";

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

	/**
	 * A node in the dependency graph. One references exactly one predicate. This
	 * means that all rule heads deriving the same predicate will be condensed into
	 * the same graph node. In some cases this results in more "conservative"
	 * results in stratification analysis, where some rules will not be evaluated
	 * up-front, although that would be possible.
	 * 
	 * Note that constraints are represented by one dummy predicate (named
	 * "constr_{num}"). Each constraint node has a negative edge to itself to
	 * express the notation of a constraint ":- a, b." as "x :- a, b, not x.".
	 *
	 */
	public class Node {

		private final Predicate predicate;
		private final String label;
		private final boolean isConstraint;

		public Node(Predicate predicate, String label, boolean isConstraint) {
			this.predicate = predicate;
			this.label = label;
			this.isConstraint = isConstraint;
		}

		public Node(Predicate predicate, String label) {
			this(predicate, label, false);
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

		public boolean isConstraint() {
			return this.isConstraint;
		}

	}

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the
	 * value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<Node, List<Edge>> nodes = new HashMap<>();

	private int constraintNumber = 0;

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
				throw new IllegalStateException(
						"Dependency graph already contains node for constraint " + p.toString() + "!");
			}
			this.nodes.put(retVal, dependencies);
		} else {
			Atom head = rule.getHeadAtom();
			retVal = new Node(head.getPredicate(), head.getPredicate().toString());
			if (!this.nodes.containsKey(retVal)) {
				LOGGER.trace("Creating new node for predicate {}", retVal.predicate);
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
			LOGGER.trace("Creating new node for predicate {}", bodyNode.predicate);
			dependants = new ArrayList<>();
			this.nodes.put(bodyNode, dependants);
		} else {
			LOGGER.trace("Node for predicate {} already exists", bodyNode.predicate);
			dependants = this.nodes.get(bodyNode);
		}
		tmpEdge = new Edge(headNode, !lit.isNegated(), lit.isNegated() ? "-" : "+");
		if (!dependants.contains(tmpEdge)) {
			LOGGER.trace("Adding dependency: {} -> {} ({})", bodyNode.predicate, headNode.predicate,
					tmpEdge.sign ? "+" : "-");
			dependants.add(tmpEdge);
		}
	}

	private Predicate generateConstraintDummyPredicate() {
		Predicate retVal = Predicate.getInstance(
				String.format(DependencyGraph.CONSTRAINT_PREDICATE_FORMAT, this.nextConstraintNumber()), 0);
		return retVal;
	}

	private int nextConstraintNumber() {
		return ++this.constraintNumber;
	}

	public Map<Node, List<Edge>> getNodes() {
		return this.nodes;
	}

}
