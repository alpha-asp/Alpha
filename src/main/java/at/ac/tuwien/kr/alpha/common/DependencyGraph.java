package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	public class Edge {
		public final int target;
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
		public Edge(int target, boolean sign) {
			this.target = target;
			this.sign = sign;
		}
	}

	/**
	 * Maps Rule IDs (toplevel key) to outgoing edges of that node NOTE: Doing the
	 * value as List<Edge> rather than Map<Integer,Boolean> as one node may have
	 * positive and negative edges to another.
	 */
	private Map<Integer, List<Edge>> nodes = new HashMap<>();

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
	private void addNode(Integer nodeId, Edge... edges) {
		List<Edge> edgeList = new ArrayList<>();
		for (Edge e : edges) {
			edgeList.add(e);
		}
		if (this.nodes.containsKey(nodeId)) {
			throw new IllegalArgumentException("Node map already contains node id " + nodeId + "as a key!");
		}
	}

	public static DependencyGraph buildDependencyGraph(Program p) {
		DependencyGraph retVal = new DependencyGraph();
		int idx = 0;
		for(Atom fact : p.getFacts()) {
			
		}
		return null;
	}

}
