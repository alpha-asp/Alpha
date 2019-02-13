package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.Deque;
import java.util.List;
import java.util.Map;

public class DfsResult {

	private Deque<Node> finishedNodes;
	private Map<Node, List<Node>> depthFirstForest;

	public Deque<Node> getFinishedNodes() {
		return this.finishedNodes;
	}

	public void setFinishedNodes(Deque<Node> finishedNodes) {
		this.finishedNodes = finishedNodes;
	}

	public Map<Node, List<Node>> getDepthFirstForest() {
		return this.depthFirstForest;
	}

	public void setDepthFirstForest(Map<Node, List<Node>> depthFirstForest) {
		this.depthFirstForest = depthFirstForest;
	}

}
