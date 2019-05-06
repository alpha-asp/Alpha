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
 * Copyright (c) 2017-2019, the Alpha Team.
 */
// TODO ensure immutability
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

	public Node(Predicate predicate) {
		this(predicate, predicate.toString());
	}

	/**
	 * Copy-constructor - constructs a new node as a deep-copy of the passed node
	 * 
	 * @param original the node to copy
	 */
	public Node(Node original) {
		this(original.predicate, original.label, original.isConstraint);
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

}
