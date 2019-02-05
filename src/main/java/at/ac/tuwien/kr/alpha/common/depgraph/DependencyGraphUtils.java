package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DependencyGraphUtils {

	private DependencyGraphUtils() {

	}

	/**
	 * Performs a depth-first search on the given graph. During the search, the
	 * <code>NodeInfo</code> for each node is filled out, specifically, the
	 * dfsDiscoveryTime, dfsFinishTime and dfsPredecessor values for each node are
	 * set. The algorithm follows the approach outlined in "Introduction to
	 * Algortihms, 3rd. Edition" by Cormen et al. Note that no separate data
	 * structure for the discovered depth-first forest is returned as that
	 * information can be gained from the completely filled <code>NodeInfo</code>s
	 * 
	 * @param nodes an adjacency map defining the dependency graph of an ASP program
	 */
	public static void performDfs(Map<Node, List<Edge>> nodes) {
		Set<Node> discovered = new HashSet<>();
		Set<Node> finished = new HashSet<>();
		int dfsTime = 0;
		for (Node n : nodes.keySet()) {
			if (!(discovered.contains(n) || finished.contains(n))) {
				dfsTime = DependencyGraphUtils.dfsVisit(dfsTime, n, nodes, discovered, finished);
			}
		}
	}

	private static int dfsVisit(int dfsTime, Node currNode, Map<Node, List<Edge>> nodes, Set<Node> discovered, Set<Node> finished) {
		int retVal = dfsTime;
		retVal++;
		currNode.getNodeInfo().setDfsDiscoveryTime(retVal);
		discovered.add(currNode);
		Node tmpNeighbor;
		for (Edge e : nodes.get(currNode)) {
			// progress to adjacent nodes
			tmpNeighbor = e.getTarget();
			if (!(discovered.contains(tmpNeighbor) || finished.contains(tmpNeighbor))) {
				tmpNeighbor.getNodeInfo().setDfsPredecessor(currNode);
				retVal = DependencyGraphUtils.dfsVisit(retVal, tmpNeighbor, nodes, discovered, finished);
			}
		}
		retVal++;
		currNode.getNodeInfo().setDfsFinishTime(retVal);
		finished.add(currNode);
		return retVal;
	}

}
