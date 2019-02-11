package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DependencyGraphUtils {

	private DependencyGraphUtils() {

	}

	/**
	 * Performs a depth-first search on the given graph. During the search, the <code>NodeInfo</code> for each node is filled out, specifically, the
	 * dfsDiscoveryTime, dfsFinishTime and dfsPredecessor values for each node are set. The algorithm follows the approach outlined in "Introduction to
	 * Algortihms, 3rd. Edition" by Cormen et al. Note that no separate data structure for the discovered depth-first forest is returned as that information can
	 * be gained from the completely filled <code>NodeInfo</code>s
	 * 
	 * @param nodeVisitSeq an Iterable defining in which sequence nodes should be visited
	 * @param nodes        an adjacency map defining the dependency graph of an ASP program
	 * @return a Set<Node> holding all finished nodes (i.e. all nodes at the end of the DFS run)
	 */
	public static DfsResult performDfs(Iterable<Node> nodeVisitSeq, Map<Node, List<Edge>> nodes) {
		DfsResult retVal = new DfsResult();
		Set<Node> discovered = new HashSet<>();
		List<Node> finished = new ArrayList<>();
		Map<Node, List<Node>> depthFirstForest = new HashMap<>();
		depthFirstForest.put(null, new ArrayList<>());
		int dfsTime = 0;
		for (Node n : nodeVisitSeq) {
			if (!(discovered.contains(n) || finished.contains(n))) {
				depthFirstForest.get(null).add(n);
				dfsTime = DependencyGraphUtils.dfsVisit(dfsTime, n, nodes, discovered, finished, depthFirstForest);
			}
		}
		retVal.setFinishedNodes(finished);
		retVal.setDepthFirstForest(depthFirstForest);
		return retVal;
	}

	private static int dfsVisit(int dfsTime, Node currNode, Map<Node, List<Edge>> nodes, Set<Node> discovered, List<Node> finished,
			Map<Node, List<Node>> dfForest) {
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
				if (!dfForest.containsKey(currNode)) {
					dfForest.put(currNode, new ArrayList<>());
				}
				dfForest.get(currNode).add(tmpNeighbor);
				retVal = DependencyGraphUtils.dfsVisit(retVal, tmpNeighbor, nodes, discovered, finished, dfForest);
			}
		}
		retVal++;
		currNode.getNodeInfo().setDfsFinishTime(retVal);
		finished.add(currNode);
		return retVal;
	}

}
