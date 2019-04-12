package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
// TODO internalize into DependencyGraph - most stuff here depends on the dependency graph
public final class DependencyGraphUtils {

	private DependencyGraphUtils() {

	}

	/**
	 * Performs a depth-first search on the given graph. During the search, the <code>NodeInfo</code> for each node is filled out, specifically, the
	 * dfsDiscoveryTime, dfsFinishTime and dfsPredecessor values for each node are set. The algorithm follows the approach outlined in "Introduction to
	 * Algortihms, 3rd. Edition" by Cormen et al. Note that no separate data structure for the discovered depth-first forest is returned as that information can
	 * be gained from the completely filled <code>NodeInfo</code>s
	 * 
	 * @param nodeVisitSeq an Iterator defining in which sequence nodes should be visited
	 * @param nodes        an adjacency map defining the dependency graph of an ASP program
	 * @return a Set<Node> holding all finished nodes (i.e. all nodes at the end of the DFS run)
	 */
	public static DfsResult performDfs(Iterator<Node> nodeVisitIt, Map<Node, List<Edge>> nodes) {
		DfsResult retVal = new DfsResult();
		Set<Node> discovered = new HashSet<>();
		Deque<Node> finished = new LinkedList<>();
		Map<Node, List<Node>> depthFirstForest = new HashMap<>();
		depthFirstForest.put(null, new ArrayList<>());
		int dfsTime = 0;
		Node tmp;
		while (nodeVisitIt.hasNext()) {
			tmp = nodeVisitIt.next();
			if (!(discovered.contains(tmp) || finished.contains(tmp))) {
				depthFirstForest.get(null).add(tmp);
				dfsTime = DependencyGraphUtils.dfsVisit(dfsTime, tmp, nodes, discovered, finished, depthFirstForest);
			}
		}
		retVal.setFinishedNodes(finished);
		retVal.setDepthFirstForest(depthFirstForest);
		return retVal;
	}

	private static int dfsVisit(int dfsTime, Node currNode, Map<Node, List<Edge>> nodes, Set<Node> discovered, Deque<Node> finished,
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

	public static boolean isReachableFrom(Node dest, Node src, DependencyGraph dg) {
		return DependencyGraphUtils.isReachableFrom(dest, src, dg, new ArrayList<>());
	}

	private static boolean isReachableFrom(Node dest, Node src, DependencyGraph dg, List<Node> discovered) {
		List<Edge> outgoingEdges;
		if (src.equals(dest)) {
			return true;
		}
		if ((outgoingEdges = dg.getNodes().get(src)) == null) {
			return false;
		}
		discovered.add(src);
		// we wanna do BFS here, therefore use 2 loops
		for (Edge edge : outgoingEdges) {
			if (edge.getTarget().equals(dest)) {
				return true;
			}
		}
		for (Edge tmp : outgoingEdges) {
			if (discovered.contains(tmp.getTarget())) {
				// cycle found
				continue;
			}
			if (DependencyGraphUtils.isReachableFrom(dest, tmp.getTarget(), dg, discovered)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given nodes are strongly connected within the given dependency graph. Strongly connected means every node in the given list is
	 * reachable from every other node in the list and vice versa.
	 * 
	 * @param connectedNodes the nodes to check
	 * @param dg             the dependency graph in which to check
	 * @return true if the given nodes are strongly connected, false otherwise
	 */
	public static boolean areStronglyConnected(List<Node> connectedNodes, DependencyGraph dg) {
		for (Node n1 : connectedNodes) {
			for (Node n2 : connectedNodes) {
				if (!(DependencyGraphUtils.isReachableFrom(n2, n1, dg) && DependencyGraphUtils.isReachableFrom(n1, n2, dg))) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isStronglyConnectedComponent(List<Node> componentNodes, DependencyGraph dg) {
		if (!DependencyGraphUtils.areStronglyConnected(componentNodes, dg)) {
			return false;
		}
		// now check if the given set is maximal
		List<Node> lst = new ArrayList<>();
		for (Node n : componentNodes) {
			lst.add(n);
		}
		for (Node n : dg.getNodes().keySet()) {
			if (lst.contains(n)) {
				continue;
			}
			lst.add(n);
			if (DependencyGraphUtils.areStronglyConnected(lst, dg)) {
				// not a strongly connected component if there is a bigger set which is strongly connected
				return false;
			}
		}
		return true;
	}

}
