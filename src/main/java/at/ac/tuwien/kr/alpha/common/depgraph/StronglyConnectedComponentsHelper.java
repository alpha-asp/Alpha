package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StronglyConnectedComponentsHelper {

	private DepthFirstSearchHelper dfsHelper = new DepthFirstSearchHelper();

	public Map<Integer, List<Node>> findStronglyConnectedComponents(DependencyGraph dg) {
		DfsResult intermediateResult = this.dfsHelper.performDfs(dg.getNodes());
		Map<Node, List<Edge>> transposedNodes = this.transposeGraph(dg.getNodes());
		Deque<Node> finishedNodes = intermediateResult.getFinishedNodes();
		DfsResult finalResult = this.dfsHelper.performDfs(finishedNodes.descendingIterator(), transposedNodes);
		Map<Integer, List<Node>> componentMap = this.extractComponents(finalResult);
		return componentMap;
	}

	private Map<Integer, List<Node>> extractComponents(DfsResult dfsResult) {
		int componentCnt = 0;
		Map<Integer, List<Node>> retVal = new HashMap<>();
		List<Node> tmpComponentMembers;
		for (Node componentRoot : dfsResult.getDepthFirstForest().get(null)) {
			tmpComponentMembers = new ArrayList<>();
			this.addComponentMembers(componentRoot, dfsResult.getDepthFirstForest(), tmpComponentMembers, componentCnt);
			retVal.put(componentCnt, tmpComponentMembers);
			componentCnt++;
		}
		return retVal;
	}

	private void addComponentMembers(Node depthFirstTreeNode, Map<Node, List<Node>> depthFirstForest, List<Node> componentMembers, int componentId) {
		depthFirstTreeNode.getNodeInfo().setComponentId(componentId);
		componentMembers.add(depthFirstTreeNode);
		List<Node> children;
		if ((children = depthFirstForest.get(depthFirstTreeNode)) == null) {
			return;
		}
		for (Node n : children) {
			this.addComponentMembers(n, depthFirstForest, componentMembers, componentId);
		}
	}

	/**
	 * (Re-)Writes the <code>transposedNodes</code> of this <code>DependencyGraph</code>
	 */
	private Map<Node, List<Edge>> transposeGraph(Map<Node, List<Edge>> nodes) {
		Map<Node, List<Edge>> transposed = new HashMap<>();
		Node srcNode;
		Node targetNode;
		for (Map.Entry<Node, List<Edge>> entry : nodes.entrySet()) {
			srcNode = entry.getKey();
			if (!transposed.containsKey(srcNode)) {
				transposed.put(srcNode, new ArrayList<>());
			}
			for (Edge e : entry.getValue()) {
				targetNode = e.getTarget();
				if (!transposed.containsKey(e.getTarget())) {
					transposed.put(targetNode, new ArrayList<>());
				}
				transposed.get(targetNode).add(new Edge(entry.getKey(), e.getSign(), e.getLabel()));
			}
		}
		return transposed;
	}

}
