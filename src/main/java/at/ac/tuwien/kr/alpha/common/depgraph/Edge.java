package at.ac.tuwien.kr.alpha.common.depgraph;

/**
 * An edge in a dependency graph.
 */
public class Edge {

	private final Node target;
	private final boolean sign;
	private final String label;

	/**
	 * Creates a new edge of a dependency graph. Read as "target depends on source" Sign indicates if the dependency is positive or negative (target node
	 * depends on default negated atom). NOTE: Working assumption is to treat strong negation as a positive dependency
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
		return ("" + this.target.getPredicate().toString() + Boolean.toString(this.sign)).hashCode();
	}

	@Override
	public String toString() {
		return "(" + (this.sign ? "+" : "-") + ") ---> " + this.target.toString();
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
