package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class DependencyGraphUtils {

	private DependencyGraphUtils() {

	}

	public static void performDfs(Map<Node, List<Edge>> nodes) {
		HashSet<Node> discovered = new HashSet<>();
		HashSet<Node> finished = new HashSet<>();
		int dfsTime = 0;
		for (Node n : nodes.keySet()) {
			if (!(discovered.contains(n) || finished.contains(n))) {
				dfsTime = DependencyGraphUtils.dfsVisit(dfsTime, n, nodes, discovered, finished);
			}
		}
	}

	private static int dfsVisit(int dfsTime, Node currNode, Map<Node, List<Edge>> nodes, HashSet<Node> discovered, HashSet<Node> finished) {
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
