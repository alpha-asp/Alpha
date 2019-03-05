package at.ac.tuwien.kr.alpha.common.depgraph;

import at.ac.tuwien.kr.alpha.common.Predicate;

/**
 * A node in a dependency graph. One node references exactly one predicate. This means that all rule heads deriving the same predicate will be condensed into
 * the same graph node. In some cases this results in more "conservative" results in stratification analysis, where some rules will not be evaluated up-front,
 * although that would be possible.
 * 
 * Note that constraints are represented by one dummy predicate (named "constr_{num}"). Each constraint node has a negative edge to itself to express the
 * notation of a constraint ":- a, b." as "x :- a, b, not x.".
 *
 */
// TODO ensure immutability
public class Node {

	public static class NodeInfo {

		private Node dfsPredecessor;
		private int dfsDiscoveryTime;
		private int dfsFinishTime;
		private int componentId;

		public NodeInfo() {

		}

		public NodeInfo(NodeInfo original) {
			this.dfsDiscoveryTime = original.dfsDiscoveryTime;
			this.dfsFinishTime = original.dfsFinishTime;
		}

		public int getDfsDiscoveryTime() {
			return this.dfsDiscoveryTime;
		}

		public void setDfsDiscoveryTime(int dfsDiscoveryTime) {
			this.dfsDiscoveryTime = dfsDiscoveryTime;
		}

		public int getDfsFinishTime() {
			return this.dfsFinishTime;
		}

		public void setDfsFinishTime(int dfsFinishTime) {
			this.dfsFinishTime = dfsFinishTime;
		}

		public Node getDfsPredecessor() {
			return this.dfsPredecessor;
		}

		public void setDfsPredecessor(Node dfsPredecessor) {
			this.dfsPredecessor = dfsPredecessor;
		}

		public int getComponentId() {
			return this.componentId;
		}

		public void setComponentId(int componentId) {
			this.componentId = componentId;
		}

	}

	private final Predicate predicate;
	private final String label;
	private final boolean isConstraint;
	private final NodeInfo nodeInfo;

	public Node(Predicate predicate, String label, boolean isConstraint, NodeInfo info) {
		this.predicate = predicate;
		this.label = label;
		this.isConstraint = isConstraint;
		this.nodeInfo = info;
	}

	public Node(Predicate predicate, String label, boolean isConstraint) {
		this(predicate, label, isConstraint, new NodeInfo());
	}

	public Node(Predicate predicate, String label) {
		this(predicate, label, false);
	}

	public Node(Predicate predicate) {
		this(predicate, predicate.toString());
	}

	/**
	 * Copy-constructor - constructs a new node as a deep-copy of the passed node
	 * 
	 * @param original the node to copy
	 */
	public Node(Node original) {
		this(original.predicate, original.label, original.isConstraint, new NodeInfo(original.nodeInfo));
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

	@Override
	public String toString() {
		return "Node{" + this.predicate.toString() + "}";
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

	public NodeInfo getNodeInfo() {
		return this.nodeInfo;
	}

}
