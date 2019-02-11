package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.List;
import java.util.Map;

public class DfsResult {

	private List<Node> finishedNodes;
	private Map<Node, List<Node>> depthFirstForest;

	public List<Node> getFinishedNodes() {
		return this.finishedNodes;
	}

	public void setFinishedNodes(List<Node> finishedNodes) {
		this.finishedNodes = finishedNodes;
	}

	public Map<Node, List<Node>> getDepthFirstForest() {
		return this.depthFirstForest;
	}

	public void setDepthFirstForest(Map<Node, List<Node>> depthFirstForest) {
		this.depthFirstForest = depthFirstForest;
	}

}
