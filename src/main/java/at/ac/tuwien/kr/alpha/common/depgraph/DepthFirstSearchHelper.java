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

public class DepthFirstSearchHelper {

	private Set<Node> discoveredNodes;
	private Deque<Node> finishedNodes;
	private Map<Node, List<Node>> depthFirstForest;

	private void reset() {
		this.discoveredNodes = new HashSet<>();
		this.finishedNodes = new LinkedList<>();
		this.depthFirstForest = new HashMap<>();
		this.depthFirstForest.put(null, new ArrayList<>());
	}

	public DfsResult performDfs(Map<Node, List<Edge>> nodes) {
		return this.performDfs(nodes.keySet().iterator(), nodes);
	}

	/**
	 * Performs a depth-first search on the given graph. During the search, the <code>NodeInfo</code> for each node is filled out, specifically, the
	 * dfsDiscoveryTime, dfsFinishTime and dfsPredecessor values for each node are set. The algorithm follows the approach outlined in "Introduction to
	 * Algortihms, 3rd. Edition" by Cormen et al. Note that no separate data structure for the discovered depth-first forest is returned as that information can
	 * be gained from the completely filled <code>NodeInfo</code>s
	 * 
	 * @param nodeVisitIt an Iterator defining in which sequence nodes should be visited
	 * @param nodes       an adjacency map defining the dependency graph of an ASP program
	 * @return a Set<Node> holding all finished nodes (i.e. all nodes at the end of the DFS run)
	 */
	public DfsResult performDfs(Iterator<Node> nodeVisitIt, Map<Node, List<Edge>> nodes) {
		this.reset();
		DfsResult retVal = new DfsResult();
		Node tmp;
		while (nodeVisitIt.hasNext()) {
			tmp = nodeVisitIt.next();
			if (!(this.discoveredNodes.contains(tmp) || this.finishedNodes.contains(tmp))) {
				this.depthFirstForest.get(null).add(tmp);
				this.dfsVisit(tmp, nodes);
			}
		}
		retVal.setFinishedNodes(this.finishedNodes);
		retVal.setDepthFirstForest(this.depthFirstForest);
		return retVal;
	}

	private void dfsVisit(Node currNode, Map<Node, List<Edge>> nodes) {
		this.discoveredNodes.add(currNode);
		Node tmpNeighbor;
		for (Edge e : nodes.get(currNode)) {
			// progress to adjacent nodes
			tmpNeighbor = e.getTarget();
			if (!(this.discoveredNodes.contains(tmpNeighbor) || this.finishedNodes.contains(tmpNeighbor))) {
				if (!this.depthFirstForest.containsKey(currNode)) {
					this.depthFirstForest.put(currNode, new ArrayList<>());
				}
				this.depthFirstForest.get(currNode).add(tmpNeighbor);
				this.dfsVisit(tmpNeighbor, nodes);
			}
		}
		this.finishedNodes.add(currNode);
	}

}
